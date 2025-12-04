package ru.practicum.test_task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Адрес отеля")
public class AddressRequest {

    @Schema(description = "Номер дома", nullable = true)
    private String houseNumber;

    @Schema(description = "Улица", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Street is required")
    private String street;

    @Schema(description = "Город",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "Страна", nullable = true)
    private String county;

    @Schema(description = "Почтовый индекс", nullable = true)
    private String postCode;
}