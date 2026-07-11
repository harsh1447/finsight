package com.finsight.finsight_backend.repository;

import com.finsight.finsight_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, String type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumAmountByUserIdAndTypeAndMonth(@Param("userId") Long userId, @Param("type") String type, @Param("month") int month, @Param("year") int year);
}
