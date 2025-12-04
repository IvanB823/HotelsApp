package ru.practicum.test_task.dto.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO контактной информации для ответа")
public class ContactDto {

    @Schema(description = "Телефон", nullable = true)
    private String phone;

    @Schema(description = "Email", nullable = true)
    private String email;

    public ContactDto() {}
}