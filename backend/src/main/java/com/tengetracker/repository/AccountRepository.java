package com.tengetracker.repository;

import com.tengetracker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByIdAsc(Long userId);

    long countByUserId(Long userId);
}
