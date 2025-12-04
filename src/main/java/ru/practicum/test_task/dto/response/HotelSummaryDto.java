package ru.practicum.test_task.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.test_task.model.Address;
import ru.practicum.test_task.model.Contact;
import ru.practicum.test_task.model.Hotel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Краткая информация об отеле")
public class HotelSummaryDto {

    @Schema(description = "ID отеля")
    private Long id;

    @Schema(description = "Название отеля")
    private String name;

    @Schema(
            description = "Описание отеля",
            nullable = true
    )
    private String description;

    @Schema(
            description = "Форматированный адрес",
            nullable = true
    )
    private String address;

    @Schema(description = "Телефон отеля", nullable = true)
    private String phone;

    public HotelSummaryDto(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.description = hotel.getDescription();
        this.address = formatAddress(hotel.getAddress());
        this.phone = extractPhone(hotel.getContacts());
    }

    private String formatAddress(Address address) {
        if (address == null) return null;

        return String.format("%s %s, %s, %s, %s",
                address.getHouseNumber(),
                address.getStreet(),
                address.getCity(),
                address.getCounty(),
                address.getPostCode()
        );
    }

    private String extractPhone(List<Contact> contacts) {
        if (contacts == null) return null;

        return contacts.stream()
                .filter(contact -> "PHONE".equals(contact.getContactType()))
                .map(Contact::getContactValue)
                .findFirst()
                .orElse(null);
    }
}