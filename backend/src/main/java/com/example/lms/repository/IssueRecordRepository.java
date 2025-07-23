package com.example.lms.repository;

import com.example.lms.model.IssueRecord;
import com.example.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByUser(User user);
} 