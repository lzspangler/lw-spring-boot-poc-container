package com.redhat.lightwell.repository;

import java.util.List;
import java.util.Optional;
import com.redhat.lightwell.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);
}
