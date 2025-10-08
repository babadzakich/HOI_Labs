package ru.nsu.chuvashov.serverdtos;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public record ClientKey(PublicKey key, X509Certificate certificate) {

}
