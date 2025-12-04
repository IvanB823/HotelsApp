package ru.practicum.test_task.dto.response;

import lombok.Data;
import ru.practicum.test_task.model.Amenity;
import ru.practicum.test_task.model.Contact;
import ru.practicum.test_task.model.Hotel;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "Детальная информация об отеле")
public class HotelDetailedDto {

    @Schema(description = "ID отеля")
    private Long id;

    @Schema(description = "Название отеля")
    private String name;

    @Schema(description = "Бренд отеля", nullable = true)
    private String brand;

    @Schema(description = "Адрес отеля")
    private AddressDto address;

    @Schema(description = "Контактная информация")
    private ContactDto contacts;

    @Schema(description = "Время заезда и выезда", nullable = true)
    private ArrivalTimeDto arrivalTime;

    @ArraySchema(
            schema = @Schema(description = "Удобство"),
            arraySchema = @Schema(
                    description = "Список удобств отеля"
            )
    )
    private List<String> amenities;

    public HotelDetailedDto(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.brand = hotel.getBrand();
        this.address = hotel.getAddress() != null ? new AddressDto(hotel.getAddress()) : null;
        this.contacts = extractContacts(hotel.getContacts());
        this.arrivalTime = hotel.getArrivalTime() != null ? new ArrivalTimeDto(hotel.getArrivalTime()) : null;
        this.amenities = extractAmenityNames(hotel.getAmenities());
    }

    private ContactDto extractContacts(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) return null;

        ContactDto contactDto = new ContactDto();
        contacts.forEach(contact -> {
            if ("PHONE".equals(contact.getContactType())) {
                contactDto.setPhone(contact.getContactValue());
            } else if ("EMAIL".equals(contact.getContactType())) {
                contactDto.setEmail(contact.getContactValue());
            }
        });
        return contactDto;
    }

    private List<String> extractAmenityNames(List<Amenity> amenities) {
        if (amenities == null) return Collections.emptyList();

        return amenities.stream()
                .map(Amenity::getName)
                .collect(Collectors.toList());
    }
}