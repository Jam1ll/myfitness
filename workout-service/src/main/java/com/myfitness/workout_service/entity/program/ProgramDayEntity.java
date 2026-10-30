package com.myfitness.workout_service.entity.program;

import com.myfitness.workout_service.entity.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "program_days")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "routine_id")
    private UUID routineId;

    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_program_id", nullable = false)
    private WeeklyProgramEntity weeklyProgramEntity;
}
