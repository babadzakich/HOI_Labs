package ru.nsu.chuvashov.serverdtos;

import java.util.concurrent.CompletableFuture;

public record Task(String name, CompletableFuture<ClientKey> client) {
}
