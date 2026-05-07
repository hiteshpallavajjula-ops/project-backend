package com.wellness.platform.repository;

import com.wellness.platform.model.ActivityLog;
import com.wellness.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserOrderByLogDateDesc(User user);
    List<ActivityLog> findAllByOrderByLogDateDesc();

    @Query("SELECT SUM(a.stepCount) FROM ActivityLog a WHERE a.user = :user")
    Long sumStepsByUser(User user);

    @Query("SELECT SUM(a.workoutMinutes) FROM ActivityLog a WHERE a.user = :user")
    Long sumWorkoutMinutesByUser(User user);
}
