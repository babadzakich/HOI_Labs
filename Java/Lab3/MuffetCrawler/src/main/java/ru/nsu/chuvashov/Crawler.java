package ru.nsu.chuvashov;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


public class Crawler {
    private final String BASE_URL;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> messages = new CopyOnWriteArrayList<>();

    public Crawler(int port) {
        this.BASE_URL = "http://localhost:" + port;
    }

    public void run(){

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            String url = "";
            CompletableFuture.runAsync(() -> crawl(url, executorService), executorService).join();
        }

        System.out.println("Crawler is finished");
        Collections.sort(messages);
        System.out.println(messages);
        System.out.println(messages.size());
    }

    private void crawl(String url, ExecutorService executorService) {
        try {
            List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();
            System.out.println("Executing request " + url);

            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/" + url)).GET().build();
            System.out.println("Executing HTTP request " + request.uri());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode data = mapper.readTree(response.body());
                String content = data.get("message").asText();
                System.out.println("From url " + url + " Got content: " + content);
                JsonNode links = data.get("successors");
                System.out.println("From url " + url + " Got links: " + links);

                messages.add(content);

                for (JsonNode link : links) {
                    futures.add(CompletableFuture.runAsync(() -> crawl(link.asText(), executorService), executorService));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
