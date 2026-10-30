package com.myfitness.workout_service.dto.program;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateWeeklyProgramRequest(

        @NotBlank(message = "Weekly Program's name can not be blank")
        @Size(min = 3, max = 100, message = "Program's name must have between 3 and 100 characters")
        String name,

        @NotEmpty(message = "Program Day's items list can not be empty")
        List<@Valid ProgramDayItemDto> programDays
) {
}