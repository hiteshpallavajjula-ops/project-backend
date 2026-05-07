package com.wellness.platform.repository;

import com.wellness.platform.model.WellnessProgram;
import com.wellness.platform.model.ProgramCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WellnessProgramRepository extends JpaRepository<WellnessProgram, Long> {
    List<WellnessProgram> findByActiveTrue();
    List<WellnessProgram> findByCategoryAndActiveTrue(ProgramCategory category);
}
