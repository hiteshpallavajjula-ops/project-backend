package com.wellness.platform.repository;

import com.wellness.platform.model.AdminSuggestion;
import com.wellness.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminSuggestionRepository extends JpaRepository<AdminSuggestion, Long> {
    List<AdminSuggestion> findByStudentOrderByCreatedAtDesc(User student);
    List<AdminSuggestion> findByStudentAndReadFalse(User student);
    List<AdminSuggestion> findAllByOrderByCreatedAtDesc();
    List<AdminSuggestion> findByAdminOrderByCreatedAtDesc(User admin);
}
