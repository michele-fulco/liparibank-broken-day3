#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  LipariBank Broken Project — Day 3
#  Compilazione e avvio
# ─────────────────────────────────────────────────────────────────────────────

set -e

SOURCES=(
  src/com/lipari/bank/model/CustomerType.java
  src/com/lipari/bank/model/TransactionType.java
  src/com/lipari/bank/model/Customer.java
  src/com/lipari/bank/model/Transaction.java
  src/com/lipari/bank/model/Account.java
  src/com/lipari/bank/model/CheckingAccount.java
  src/com/lipari/bank/model/SavingsAccount.java
  src/com/lipari/bank/pattern/BankCommand.java
  src/com/lipari/bank/pattern/DepositCommand.java
  src/com/lipari/bank/pattern/WithdrawCommand.java
  src/com/lipari/bank/pattern/TransferCommand.java
  src/com/lipari/bank/pattern/BankConfiguration.java
  src/com/lipari/bank/pattern/AccountFactory.java
  src/com/lipari/bank/service/ReportingService.java
  src/com/lipari/bank/service/ConcurrentBatchService.java
  src/com/lipari/bank/cli/BankConsoleDayThree.java
)

echo "==> Creazione cartella output..."
mkdir -p out

echo "==> Compilazione con $(java -version 2>&1 | head -1)..."
javac -d out "${SOURCES[@]}"

echo "==> Compilazione completata. Avvio applicazione..."
echo "─────────────────────────────────────────────────────────────────────────────"
java -cp out com.lipari.bank.cli.BankConsoleDayThree
