package ru.practicum.test_task.dto.response;

import lombok.Data;
import ru.practicum.test_task.model.ArrivalTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO времени заезда и выезда для ответа")
public class ArrivalTimeDto {

    @Schema(description = "Время заезда (check-in)")
    private String checkIn;

    @Schema(description = "Время выезда (check-out)", nullable = true)
    private String checkOut;

    public ArrivalTimeDto(ArrivalTime arrivalTime) {
        this.checkIn = arrivalTime.getCheckIn() != null ? arrivalTime.getCheckIn().toString() : null;
        this.checkOut = arrivalTime.getCheckOut() != null ? arrivalTime.getCheckOut().toString() : null;
    }
}