package ru.practicum.test_task.dto.response;

import lombok.Data;
import ru.practicum.test_task.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO адреса для ответа")
public class AddressDto {

    @Schema(description = "Номер дома", nullable = true)
    private String houseNumber;

    @Schema(description = "Улица")
    private String street;

    @Schema(description = "Город")
    private String city;

    @Schema(description = "Страна", nullable = true)
    private String county;

    @Schema(description = "Почтовый индекс", nullable = true)
    private String postCode;

    public AddressDto(Address address) {
        this.houseNumber = address.getHouseNumber();
        this.street = address.getStreet();
        this.city = address.getCity();
        this.county = address.getCounty();
        this.postCode = address.getPostCode();
    }
}