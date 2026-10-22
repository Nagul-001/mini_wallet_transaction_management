package com.example.wallet.service;

import com.example.wallet.dto.*;
import com.example.wallet.entity.*;
import com.example.wallet.exception.*;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (walletRepository.existsByEmailIgnoreCase(normalizedEmail)) throw new DuplicateEmailException(normalizedEmail);
        Wallet wallet = new Wallet(request.name().trim(), normalizedEmail, request.initialBalance());
        try {
            return WalletResponse.from(walletRepository.saveAndFlush(wallet));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(normalizedEmail);
        }
    }

    @Transactional
    public DepositResponse deposit(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdForUpdate(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
        BigDecimal previous = wallet.getBalance();
        BigDecimal current = previous.add(amount);
        wallet.setBalance(current);
        transactionRepository.save(new WalletTransaction(wallet, TransactionType.DEPOSIT, amount, TransactionStatus.SUCCESS, null));
        walletRepository.save(wallet);
        return new DepositResponse(walletId, previous, amount, current);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        Long fromId = request.fromWalletId();
        Long toId = request.toWalletId();
        if (fromId.equals(toId)) throw new SameWalletTransferException();

        List<Long> ids = new ArrayList<>(List.of(fromId, toId));
        ids.sort(Comparator.naturalOrder());
        Wallet first = walletRepository.findByIdForUpdate(ids.get(0)).orElseThrow(() -> new WalletNotFoundException(ids.get(0)));
        Wallet second = walletRepository.findByIdForUpdate(ids.get(1)).orElseThrow(() -> new WalletNotFoundException(ids.get(1)));
        Wallet sender = first.getId().equals(fromId) ? first : second;
        Wallet receiver = first.getId().equals(toId) ? first : second;

        if (sender.getBalance().compareTo(request.amount()) < 0) throw new InsufficientBalanceException();
        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));

        String referenceId = UUID.randomUUID().toString();
        WalletTransaction out = transactionRepository.save(new WalletTransaction(sender, TransactionType.TRANSFER_OUT,
                request.amount(), TransactionStatus.SUCCESS, referenceId));
        transactionRepository.save(new WalletTransaction(receiver, TransactionType.TRANSFER_IN,
                request.amount(), TransactionStatus.SUCCESS, referenceId));
        walletRepository.save(sender);
        walletRepository.save(receiver);
        return new TransferResponse(out.getId(), fromId, toId, request.amount(), TransactionStatus.SUCCESS.name());
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId) {
        return WalletResponse.from(walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId)));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long walletId) {
        if (!walletRepository.existsById(walletId)) throw new WalletNotFoundException(walletId);
        return transactionRepository.findByWallet_IdOrderByCreatedAtDescIdDesc(walletId).stream()
                .map(TransactionResponse::from).toList();
    }
}
