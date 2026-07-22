package com.redhat.lightwell.repository;

import java.util.List;
import com.redhat.lightwell.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByAccountId(Long accountId);
}
