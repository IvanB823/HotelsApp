package ru.practicum.test_task.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Запрос на создание нового отеля")
public class CreateHotelRequest {

    @Schema(
            description = "Название отеля",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    @NotBlank(message = "Hotel name is required")
    private String name;

    @Schema(description = "Описание отеля", nullable = true)
    private String description;

    @Schema(description = "Бренд отеля", nullable = true)
    private String brand;

    @Schema(description = "Адрес отеля", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "Address is required")
    private AddressRequest address;

    @Schema(description = "Контактная информация", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "Contacts are required")
    private ContactRequest contacts;

    @Schema(description = "Время заезда/выезда", nullable = true)
    @Valid
    private ArrivalTimeRequest arrivalTime;
}