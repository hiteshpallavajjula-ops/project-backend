package com.wellness.platform.service;

import com.wellness.platform.model.*;
import com.wellness.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with default wellness programs on startup if none exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private WellnessProgramRepository programRepository;

    @Override
    public void run(String... args) {
        if (programRepository.count() == 0) {
            List<WellnessProgram> programs = List.of(
                WellnessProgram.builder()
                    .title("7-Day Step Challenge")
                    .description("A beginner-friendly program focused on building the habit of walking 10,000 steps per day. Track your steps, earn badges, and improve cardiovascular health progressively.")
                    .category(ProgramCategory.FITNESS)
                    .duration("7 Days")
                    .difficulty("Beginner")
                    .price(0.0)
                    .highlights("Daily step goals,Progress tracking,Motivational reminders,Heart health tips")
                    .active(true)
                    .build(),

                WellnessProgram.builder()
                    .title("Campus Strength Training")
                    .description("A structured 4-week strength training program designed for students with limited equipment. Focuses on bodyweight exercises, resistance bands, and dumbbell routines you can do in your dorm room or campus gym.")
                    .category(ProgramCategory.WORKOUT)
                    .duration("4 Weeks")
                    .difficulty("Intermediate")
                    .price(199.0)
                    .highlights("3x/week workouts,Video guides,Muscle group rotation,Rest day recovery plans")
                    .active(true)
                    .build(),

                WellnessProgram.builder()
                    .title("Student Nutrition & Diet Plan")
                    .description("A comprehensive 30-day nutrition guide tailored for students on a budget. Covers meal prep, macros, calorie tracking, and healthy eating habits that fuel your academic performance.")
                    .category(ProgramCategory.DIET)
                    .duration("30 Days")
                    .difficulty("Beginner")
                    .price(149.0)
                    .highlights("Daily meal plans,Calorie & macro tracking,Budget-friendly recipes,Supplement guidance")
                    .active(true)
                    .build(),

                WellnessProgram.builder()
                    .title("Mind & Stress Relief")
                    .description("A 3-week mental wellness program combining guided meditation, journaling, breathing exercises, and study-life balance techniques to help students manage stress, anxiety, and exam pressure.")
                    .category(ProgramCategory.MENTAL_HEALTH)
                    .duration("3 Weeks")
                    .difficulty("Beginner")
                    .price(99.0)
                    .highlights("Daily guided meditation,Journaling prompts,Breathing exercises,Sleep hygiene tips")
                    .active(true)
                    .build()
            );
            programRepository.saveAll(programs);
            System.out.println("✅ Default wellness programs seeded.");
        }
    }
}
