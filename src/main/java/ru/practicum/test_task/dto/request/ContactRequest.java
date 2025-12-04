package ru.practicum.test_task.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Контактная информация")
public class ContactRequest {

    @Schema(description = "Телефон", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone is required")
    private String phone;

    @Schema(description = "Email")
    @Email(message = "Email should be valid")
    private String email;
}