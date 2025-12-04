package ru.practicum.test_task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Время заезда и выезда")
public class ArrivalTimeRequest {

    @Schema(description = "Время заезда (check-in)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Check-in time is required")
    private String checkIn;

    @Schema(description = "Время выезда (check-out)", nullable = true)
    private String checkOut;
}