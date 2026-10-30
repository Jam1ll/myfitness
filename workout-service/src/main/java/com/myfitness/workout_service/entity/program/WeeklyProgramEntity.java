package com.myfitness.workout_service.entity.program;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "weekly_programs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "weeklyProgramEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramDayEntity> programDayEntities = new ArrayList<>();

    //helper
    public void addProgramDay(ProgramDayEntity programDay){
        programDayEntities.add(programDay);
        programDay.setWeeklyProgramEntity(this);
    }
}