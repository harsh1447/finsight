package com.finsight.finsight_backend.repository;

import com.finsight.finsight_backend.entity.UploadedStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UploadedStatementRepository extends JpaRepository<UploadedStatement, Long> {
    List<UploadedStatement> findByUserIdOrderByUploadedAtDesc(Long userId);
}
