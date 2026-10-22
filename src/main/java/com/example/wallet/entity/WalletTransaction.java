package com.example.wallet.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transaction", indexes = {
        @Index(name = "idx_wallet_transaction_wallet_created", columnList = "wallet_id, created_at")
})
@Check(constraints = "amount > 0")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_wallet"))
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected WalletTransaction() {}

    public WalletTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
                             TransactionStatus status, String referenceId) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.referenceId = referenceId;
    }

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Wallet getWallet() { return wallet; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public String getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
