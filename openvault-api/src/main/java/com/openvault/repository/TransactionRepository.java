package com.openvault.repository;

import com.openvault.entity.Transaction;
import com.openvault.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Transactions d'un compte (entrantes et sortantes)
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.timestamp DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
    
    // Dernières transactions
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.timestamp DESC")
    List<Transaction> findTop10ByAccountId(@Param("accountId") Long accountId, Pageable pageable);
    
    // Filtrer par type
    List<Transaction> findByTypeAndFromAccountIdOrToAccountId(
        TransactionType type, 
        Long fromAccountId, 
        Long toAccountId
    );
    
    // Filtrer par période
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.timestamp BETWEEN :start AND :end")
    List<Transaction> findByAccountIdAndDateRange(
        @Param("accountId") Long accountId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}