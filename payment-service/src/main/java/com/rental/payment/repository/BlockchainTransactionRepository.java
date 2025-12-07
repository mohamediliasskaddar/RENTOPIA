package com.rental.payment.repository;

import com.rental.payment.entity.BlockchainTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Integer> {

    List<BlockchainTransaction> findByReservationId(Integer reservationId);

    Optional<BlockchainTransaction> findByTransactionHash(String transactionHash);

    List<BlockchainTransaction> findByPaymentStatus(BlockchainTransaction.PaymentStatus status);

    List<BlockchainTransaction> findByPayerWalletAddress(String walletAddress);

    List<BlockchainTransaction> findByPayeeWalletAddress(String walletAddress);
}