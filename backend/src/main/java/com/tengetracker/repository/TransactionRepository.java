package com.tengetracker.repository;

import com.tengetracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** All transactions whose source account belongs to the user. */
    List<Transaction> findByAccount_UserIdOrderByDateDescIdDesc(Long userId);

    List<Transaction> findByAccountIdOrTargetAccountIdOrderByDateDescIdDesc(Long accountId, Long targetAccountId);

    List<Transaction> findByCategoryIdAndAccount_UserIdOrderByDateDescIdDesc(Long categoryId, Long userId);

    List<Transaction> findByTags_IdAndAccount_UserIdOrderByDateDescIdDesc(Long tagId, Long userId);

    List<Transaction> findByTags_Id(Long tagId);

    long countByAccount_UserId(Long userId);

    boolean existsByAccountIdOrTargetAccountId(Long accountId, Long targetAccountId);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.userId = :userId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("userId") Long userId,
                                              @Param("type") String type,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.userId = :userId AND t.categoryId = :categoryId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumUserExpensesByCategoryAndDateBetween(@Param("userId") Long userId,
                                                       @Param("categoryId") Long categoryId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}
