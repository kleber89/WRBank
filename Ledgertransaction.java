package com.remesas.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ledger_transactions")
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Clave de idempotencia provista por quien origina la operación (módulo transfer, fx, etc). */
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    /** TRANSFER, DEPOSIT, WITHDRAWAL, FX_CONVERSION, FEE, REVERSAL... */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    /** Si esta transacción reversa otra, referencia el id original. */
    @Column(name = "reverses_transaction_id")
    private UUID reversesTransactionId;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LedgerEntry> entries = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "posted_at")
    private Instant postedAt;

    protected LedgerTransaction() {
        // constructor para JPA
    }

    public LedgerTransaction(String idempotencyKey, String type, String description) {
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.description = description;
    }

    public void addEntry(LedgerEntry entry) {
        entry.setTransaction(this);
        entries.add(entry);
    }

    public void markPosted() {
        this.status = TransactionStatus.POSTED;
        this.postedAt = Instant.now();
    }

    public void markReversed() {
        this.status = TransactionStatus.REVERSED;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public List<LedgerEntry> getEntries() { return entries; }
    public Instant getCreatedAt() { return createdAt; }
}