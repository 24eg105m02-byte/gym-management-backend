package com.gym.repository;

import com.gym.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByWorkoutPlanId(Long workoutPlanId);
    void deleteByWorkoutPlanId(Long workoutPlanId);
}
