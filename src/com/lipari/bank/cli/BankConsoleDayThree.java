package com.lipari.bank.cli;

import com.lipari.bank.model.*;
import com.lipari.bank.pattern.*;
import com.lipari.bank.service.ConcurrentBatchService;
import com.lipari.bank.service.ReportingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry point del progetto LipariBank — Day 3.
 *
 * Esegue tre scenari di test sequenziali, ognuno dei quali
 * dimostra uno dei tre bug logici presenti nel progetto.
 */
public class BankConsoleDayThree {

    public static void main(String[] args) throws InterruptedException {
        new BankConsoleDayThree().runAllScenarios();
    }

    private void runAllScenarios() throws InterruptedException {
        printBanner();
        scenario1_streamDebugLog();
        scenario2_singletonRace();
        scenario3_withdrawRaceCondition();
        printFooter();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 1
    // ══════════════════════════════════════════════════════════════════════════

    private void scenario1_streamDebugLog() {
        sep("SCENARIO 1 — Conteggio transazioni via Stream (ReportingService)");

        Customer mario = new Customer("RSSMRA85M01H501Z", "Mario", "Rossi",  CustomerType.PRIVATE);
        Customer anna  = new Customer("BNCNNA90L50C351X", "Anna",  "Bianchi", CustomerType.BUSINESS);

        Account acc1 = AccountFactory.create(
                AccountFactory.AccountType.CHECKING, "IT-S1-001",
                new BigDecimal("5000.00"), mario);
        Account acc2 = AccountFactory.create(
                AccountFactory.AccountType.SAVINGS, "IT-S1-002",
                new BigDecimal("10000.00"), anna);

        // 3 depositi totali, 2 prelievi
        acc1.deposit(new BigDecimal("100.00"));
        acc1.deposit(new BigDecimal("200.00"));
        acc1.withdraw(new BigDecimal("50.00"));
        acc2.deposit(new BigDecimal("300.00"));
        acc2.withdraw(new BigDecimal("100.00"));

        List<Account> accounts = List.of(acc1, acc2);
        ReportingService reporting = new ReportingService();

        long count1 = reporting.countTransactionsByType(accounts, TransactionType.DEPOSIT);
        long count2 = reporting.countTransactionsByType(accounts, TransactionType.DEPOSIT);
        long count3 = reporting.countTransactionsByType(accounts, TransactionType.DEPOSIT);

        System.out.println("  Depositi totali nel dataset : 3");
        System.out.printf("  1ª chiamata → conteggio: %d  %s%n",
                count1, count1 == 3 ? "✓" : "← inatteso");
        System.out.printf("  2ª chiamata → conteggio: %d  %s%n",
                count2, count2 == count1
                        ? "✓ (bug non manifesto questa volta)"
                        : "← inatteso!");
        System.out.printf("  3ª chiamata → conteggio: %d  %s%n",
                count3, count3 == count1
                        ? "✓"
                        : "← " + count3 + " invece di 3!");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 2
    // ══════════════════════════════════════════════════════════════════════════

    private void scenario2_singletonRace() throws InterruptedException {
        sep("SCENARIO 2 — Singleton BankConfiguration (Double-Checked Locking)");

        final int THREAD_COUNT = 200;

        // Set thread-safe per raccogliere gli identityHashCode
        Set<Integer> observedHashCodes = ConcurrentHashMap.newKeySet();

        // I thread aspettano tutti sul startGate prima di partire insieme
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();                            // attende il via
                    BankConfiguration cfg = BankConfiguration.getInstance();
                    observedHashCodes.add(System.identityHashCode(cfg));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown();                                    // PARTENZA
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        int unique = observedHashCodes.size();
        System.out.printf("  Thread lanciati          : %d%n", THREAD_COUNT);
        System.out.printf("  Completati               : %s%n", completed ? "tutti" : "timeout!");
        System.out.printf("  Istanze uniche osservate : %d  %s%n",
                unique,
                unique == 1
                        ? "(Singleton corretto questa volta — riprova)"
                        : "← istanze multiple!");
        System.out.println("  IdentityHashCode osservati: " + observedHashCodes);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCENARIO 3
    // ══════════════════════════════════════════════════════════════════════════

    private void scenario3_withdrawRaceCondition() throws InterruptedException {
        sep("SCENARIO 3 — Race condition in Account.withdraw() (prelievi concorrenti)");

        final int        THREAD_COUNT    = 100;
        final BigDecimal WITHDRAW_AMOUNT = new BigDecimal("1000.00");
        final BigDecimal INITIAL_BALANCE = new BigDecimal("100000.00");
        final BigDecimal EXPECTED_FINAL  = BigDecimal.ZERO;

        Customer owner = new Customer("TSTTST80A01H501Z", "Stress", "Test", CustomerType.PRIVATE);
        Account account = AccountFactory.createChecking(
                "IT-RACE-001", INITIAL_BALANCE, owner, BigDecimal.ZERO);

        System.out.printf("  Saldo iniziale           : %,.2f€%n", INITIAL_BALANCE);
        System.out.printf("  Thread × importo         : %d × %,.2f€ = %,.2f€%n",
                THREAD_COUNT, WITHDRAW_AMOUNT,
                WITHDRAW_AMOUNT.multiply(BigDecimal.valueOf(THREAD_COUNT)));
        System.out.printf("  Saldo finale atteso      : %,.2f€%n", EXPECTED_FINAL);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures  = new AtomicInteger(0);

        // Tutti i thread partono esattamente nello stesso istante
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        WithdrawCommand cmd = new WithdrawCommand(WITHDRAW_AMOUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    cmd.execute(account);
                    successes.incrementAndGet();
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown();                                    // PARTENZA
        doneLatch.await(20, TimeUnit.SECONDS);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        BigDecimal finalBalance = account.getBalance();
        int txCount = account.getTransactions().size();

        System.out.printf("  Prelievi andati a buon fine: %d%n", successes.get());
        System.out.printf("  Prelievi falliti (eccezione): %d%n", failures.get());
        System.out.printf("  Transazioni registrate     : %d  (attese: %d)%n",
                txCount, THREAD_COUNT - failures.get());
        System.out.printf("  Saldo finale effettivo     : %,.2f€  %s%n",
                finalBalance,
                finalBalance.compareTo(EXPECTED_FINAL) == 0
                        ? "(corretto — race non manifesta, riprova)"
                        : "← saldo sbagliato!");

        // Calcola quanti prelievi sono stati "persi" per via della race
        BigDecimal withdrawn = INITIAL_BALANCE.subtract(finalBalance);
        BigDecimal expected  = WITHDRAW_AMOUNT.multiply(
                BigDecimal.valueOf(successes.get()));
        if (withdrawn.compareTo(expected) != 0) {
            System.out.printf("  Importo effettivamente sottratto: %,.2f€  (atteso: %,.2f€)%n",
                    withdrawn, expected);
            System.out.printf("  Prelievi 'persi' per race       : %.0f%n",
                    expected.subtract(withdrawn)
                            .divide(WITHDRAW_AMOUNT, 0, java.math.RoundingMode.FLOOR)
                            .doubleValue());
        }
    }

    // ─── Utility di stampa ───────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println("""
                ╔══════════════════════════════════════════════════════════════╗
                ║       LIPARIBANK — Broken Project Day 3                      ║
                ║       Stream API · Design Patterns · Concorrenza             ║
                ╚══════════════════════════════════════════════════════════════╝
                """);
    }

    private static void sep(String title) {
        int pad = Math.max(0, 60 - title.length());
        System.out.println("\n══ " + title + " " + "═".repeat(pad));
    }

    private static void printFooter() {
        System.out.println("""

                ════════════════════════════════════════════════════════════════
                  Hai trovato e fixato tutti e 3 i bug? Riesegui per verificare!
                ════════════════════════════════════════════════════════════════
                """);
    }
}
