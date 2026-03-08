package com.lipari.bank.service;

import com.lipari.bank.model.Account;
import com.lipari.bank.pattern.BankCommand;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Servizio che esegue batch di {@link BankCommand} in parallelo
 * tramite un pool di thread.
 */
public class ConcurrentBatchService {

    private final ExecutorService executor;

    public ConcurrentBatchService(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Esegue tutti i comandi sul conto indicato in parallelo.
     * Attende il completamento di tutti i task prima di tornare.
     *
     * @param commands lista di comandi da eseguire
     * @param account  conto su cui operare
     * @return lista di Future con l'esito di ogni comando
     */
    public List<Future<String>> executeBatch(List<BankCommand> commands, Account account) {
        List<Callable<String>> tasks = commands.stream()
                .map(cmd -> (Callable<String>) () -> {
                    try {
                        cmd.execute(account);
                        return "OK: " + cmd.describe();
                    } catch (Exception e) {
                        return "ERRORE: " + e.getMessage();
                    }
                })
                .toList();

        try {
            return executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
