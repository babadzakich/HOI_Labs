package ru.nsu.chuvashov;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import ru.nsu.chuvashov.serverdtos.ClientKey;
import ru.nsu.chuvashov.serverdtos.Task;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final int port;
    private final ConcurrentMap<String, CompletableFuture<ClientKey>> cache = new ConcurrentHashMap<>();
    private final BlockingQueue<Task> queue;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final X500Name issuer;
    private final PrivateKey privateKey;
    private final String configPath = "config.txt";
    private final String issuerKey = "Mykey.pem";

    private final KeyPairGenerator kpg;

    public Server(int port, int db_threadCount) throws NoSuchAlgorithmException {
        this.port = port;
        queue = new LinkedBlockingQueue<>();

        Security.addProvider(new BouncyCastleProvider());
        Properties config = new Properties();
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream(configPath)) {
            config.load(fis);
        } catch (IOException e) {
            config = new Properties();
            config.put("issuer.name", "CN=FATSUN");
        }

        issuer = new X500Name(config.getProperty("issuer.name"));

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(issuerKey);
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
             PEMParser pemParser = new PEMParser(isr)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            privateKey = converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
        } catch (IOException ex) {
            System.err.println("Error reading private key file");
            throw new RuntimeException("Error reading private key: " + ex.getMessage());
        }

        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(8192);
        executor = Executors.newFixedThreadPool(db_threadCount);
        for (int i = 0; i < db_threadCount; i++) {
            executor.submit(this::worker);
        }
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (running.get()) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("While working, server socket got: " + e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            var out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {
            String input = in.readLine();
            if (input == null) return;
            System.out.println(Thread.currentThread().getName() + " got word: " + input + " at " + System.currentTimeMillis());

            ClientKey result = getFuture(input).join();
            System.out.println(Thread.currentThread().getName() + " got result for " + input + " at " + System.currentTimeMillis());
            out.println(keyToPem(result.key()));
            out.println();
            out.println(certToPem(result.certificate()));
        } catch (IOException | CertificateEncodingException e) {
            System.err.println("Error handling client request: \n" +  e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error closing client: " +  e.getMessage());
            }
        }
    }

    private CompletableFuture<ClientKey> getFuture(String input) {
        return cache.computeIfAbsent(input, key -> {
            CompletableFuture<ClientKey> future1 = new CompletableFuture<>();
            queue.offer(new Task(key, future1));
            return future1;
        });
    }

    private void worker() {
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
        while (running.get()) {
            Task task = null;
            try {
                task = queue.take();
                X500Name subject = new X500Name("CN=" + task.name());
                KeyPair pair = kpg.generateKeyPair();

                SecureRandom random = new SecureRandom();
                BigInteger serial = new BigInteger(64, random);

                Date notBefore = new Date();
                Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);


                SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded());

                X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                        issuer, serial, notBefore, notAfter, subject, subPubKeyInfo);

                ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(privateKey);

                X509CertificateHolder certHolder = certBuilder.build(signer);

                X509Certificate cert = certConverter.getCertificate(certHolder);

                task.client().complete(new ClientKey(pair.getPublic(), cert));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (OperatorCreationException | CertificateException e) {
                task.client().completeExceptionally(e);
                cache.remove(task.name(), task.client());
            }
        }
        System.out.println("Worker stopped");
    }

    public void shutdown() {
        System.out.println("Shutting down server...");
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    private String keyToPem(PublicKey key) {
        Base64.Encoder mime = Base64.getMimeEncoder(64, "\n".getBytes());
        String encoded = mime.encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }

    private String certToPem(X509Certificate cert) throws CertificateEncodingException {
        Base64.Encoder mime = Base64.getMimeEncoder(64, "\n".getBytes());
        String encoded = mime.encodeToString(cert.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----";
    }
}
