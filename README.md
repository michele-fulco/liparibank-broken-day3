# 🔧 LipariBank Broken Project — Day 3

Progetto Java 21 didattico con **3 bug logici** su temi avanzati:
Stream API, Design Patterns e Concorrenza.
Il codice **compila senza errori** ma si comporta in modo scorretto a runtime.

---

## Struttura del progetto

```
liparibank-broken-day3/
├── src/com/lipari/bank/
│   ├── model/
│   │   ├── CustomerType.java
│   │   ├── TransactionType.java
│   │   ├── Customer.java
│   │   ├── Transaction.java         (record)
│   │   ├── Account.java             (sealed)
│   │   ├── CheckingAccount.java     (final)
│   │   └── SavingsAccount.java      (final)
│   ├── pattern/
│   │   ├── BankConfiguration.java   (Singleton)
│   │   ├── AccountFactory.java      (Factory)
│   │   ├── BankCommand.java         (sealed interface)
│   │   ├── DepositCommand.java      (record)
│   │   ├── WithdrawCommand.java     (record)
│   │   └── TransferCommand.java     (record)
│   ├── service/
│   │   ├── ReportingService.java    (Stream API)
│   │   └── ConcurrentBatchService.java
│   └── cli/
│       └── BankConsoleDayThree.java
├── README.md
└── compile-and-run.sh
```

---

## Compilazione e avvio

### Prerequisiti

- Java 17+ (sealed class, record, switch expression, text block)

### 1. Crea la cartella di output

```bash
mkdir -p out
```

### 2. Compila

```bash
javac -d out \
  src/com/lipari/bank/model/CustomerType.java \
  src/com/lipari/bank/model/TransactionType.java \
  src/com/lipari/bank/model/Customer.java \
  src/com/lipari/bank/model/Transaction.java \
  src/com/lipari/bank/model/Account.java \
  src/com/lipari/bank/model/CheckingAccount.java \
  src/com/lipari/bank/model/SavingsAccount.java \
  src/com/lipari/bank/pattern/BankCommand.java \
  src/com/lipari/bank/pattern/DepositCommand.java \
  src/com/lipari/bank/pattern/WithdrawCommand.java \
  src/com/lipari/bank/pattern/TransferCommand.java \
  src/com/lipari/bank/pattern/BankConfiguration.java \
  src/com/lipari/bank/pattern/AccountFactory.java \
  src/com/lipari/bank/service/ReportingService.java \
  src/com/lipari/bank/service/ConcurrentBatchService.java \
  src/com/lipari/bank/cli/BankConsoleDayThree.java
```

Oppure usa lo script:

```bash
chmod +x compile-and-run.sh
./compile-and-run.sh
```

### 3. Esegui

```bash
java -cp out com.lipari.bank.cli.BankConsoleDayThree
```

---

## 🕵️ Le tue 3 missioni

---

### MISSIONE 1 — Il conteggio delle transazioni raddoppia ad ogni chiamata

**Sintomo:** Il metodo `countTransactionsByType()` viene chiamato due volte
sullo stesso dataset. La prima chiamata restituisce il valore corretto.
La seconda chiamata restituisce **il doppio**. La terza **il triplo**. E così via.
Il numero cresce linearmente con il numero di chiamate.

---

### MISSIONE 2 — Il Singleton crea istanze multiple con thread concorrenti

**Sintomo:** Cento thread chiamano `BankConfiguration.getInstance()`
contemporaneamente. Raccogliendo gli `identityHashCode()` delle istanze
restituite, si scopre che **non sono tutti uguali**: il Singleton ha creato
più di un'istanza, violando la propria garanzia di unicità.

---

### MISSIONE 3 — Il saldo finale dopo prelievi concorrenti è sbagliato

**Sintomo:** Un conto viene creato con 100.000€. Cento thread prelevano
simultaneamente 1.000€ ciascuno (totale: 100.000€). Il saldo finale **non è 0€**:
alcuni prelievi risultano "persi" e il denaro non viene effettivamente sottratto.
Il problema è riproducibile (anche se non deterministico) su macchine multi-core.

---

## ✅ Obiettivo finale

Quando hai trovato e corretto tutti e 3 i bug, hai completato la missione!

- `countTransactionsByType()` deve restituire lo stesso valore ad ogni chiamata
- `BankConfiguration.getInstance()` deve restituire sempre la stessa istanza
- Il saldo dopo 100 prelievi da 1.000€ su 100.000€ deve essere esattamente 0,00€
