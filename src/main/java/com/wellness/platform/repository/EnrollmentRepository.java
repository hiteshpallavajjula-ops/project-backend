package com.wellness.platform.repository;

import com.wellness.platform.model.Enrollment;
import com.wellness.platform.model.User;
import com.wellness.platform.model.WellnessProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUser(User user);
    List<Enrollment> findByProgram(WellnessProgram program);
    Optional<Enrollment> findByUserAndProgram(User user, WellnessProgram program);
    boolean existsByUserAndProgram(User user, WellnessProgram program);
    long countByProgram(WellnessProgram program);
}
