package ru.practicum.test_task.service;

import ru.practicum.test_task.dto.request.CreateHotelRequest;
import ru.practicum.test_task.dto.response.HotelDetailedDto;
import ru.practicum.test_task.dto.response.HotelSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.test_task.model.*;
import ru.practicum.test_task.repository.AmenityRepository;
import ru.practicum.test_task.repository.HotelRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<HotelSummaryDto> getAllHotels() {
        log.info("Getting all hotels");
        return hotelRepository.findAll().stream()
                .map(HotelSummaryDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDetailedDto getHotelById(Long id) {
        log.info("Getting hotel by id: {}", id);

        Hotel hotel = hotelRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        if (hotel.getAmenities().isEmpty()) {
            Hotel hotelWithAmenities = hotelRepository.findByIdWithAmenities(id)
                    .orElse(hotel);
            hotel.setAmenities(hotelWithAmenities.getAmenities());
        }

        if (hotel.getContacts().isEmpty()) {
            Hotel hotelWithContacts = hotelRepository.findByIdWithContacts(id)
                    .orElse(hotel);
            hotel.setContacts(hotelWithContacts.getContacts());
        }

        return new HotelDetailedDto(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelSummaryDto> searchHotels(String name, String brand, String city, String county, List<String> amenities) {
        log.info("Searching hotels with filters - name: {}, brand: {}, city: {}, county: {}, amenities: {}",
                name, brand, city, county, amenities);

        String amenityName = (amenities != null && !amenities.isEmpty()) ? amenities.get(0) : null;

        List<Hotel> hotels = hotelRepository.findBySearchCriteria(name, brand, city, county, amenityName);
        return hotels.stream()
                .map(HotelSummaryDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HotelSummaryDto createHotel(CreateHotelRequest request) {
        log.info("Creating new hotel: {}", request.getName());

        Hotel hotel = convertToEntity(request);
        Hotel savedHotel = hotelRepository.save(hotel);

        log.info("Hotel created with id: {}", savedHotel.getId());
        return new HotelSummaryDto(savedHotel);
    }

    @Override
    @Transactional
    public void addAmenitiesToHotel(Long hotelId, List<String> amenities) {
        log.info("Adding amenities to hotel {}: {}", hotelId, amenities);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        List<Amenity> existingAmenities = amenityRepository.findByHotelId(hotelId);
        Set<String> existingAmenityNames = existingAmenities.stream()
                .map(Amenity::getName)
                .collect(Collectors.toSet());

        List<Amenity> amenitiesToAdd = amenities.stream()
                .filter(amenityName -> !existingAmenityNames.contains(amenityName))
                .map(amenityName ->
                        amenityRepository.findByName(amenityName)
                                .orElseGet(() -> {
                                    Amenity newAmenity = new Amenity();
                                    newAmenity.setName(amenityName);
                                    return amenityRepository.save(newAmenity);
                                })
                )
                .collect(Collectors.toList());

        if (!amenitiesToAdd.isEmpty()) {
            hotel.getAmenities().addAll(amenitiesToAdd);
            hotelRepository.save(hotel);
            log.info("Successfully added {} amenities to hotel {}", amenitiesToAdd.size(), hotelId);
        } else {
            log.info("No new amenities to add for hotel {} (all already exist)", hotelId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getHistogram(String param) {
        log.info("Getting histogram for parameter: {}", param);

        List<Object[]> results;
        switch (param.toLowerCase()) {
            case "brand":
                results = hotelRepository.getHistogramByBrand();
                break;
            case "city":
                results = hotelRepository.getHistogramByCity();
                break;
            case "county":
                results = hotelRepository.getHistogramByCounty();
                break;
            case "amenities":
                results = hotelRepository.getHistogramByAmenities();
                break;
            default:
                throw new IllegalArgumentException("Unsupported histogram parameter: " + param);
        }

        return results.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
    }

    private Hotel convertToEntity(CreateHotelRequest request) {
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setDescription(request.getDescription());
        hotel.setBrand(request.getBrand());

        if (request.getAddress() != null) {
            Address address = new Address();
            address.setHouseNumber(request.getAddress().getHouseNumber());
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setCounty(request.getAddress().getCounty());
            address.setPostCode(request.getAddress().getPostCode());
            address.setHotel(hotel);
            hotel.setAddress(address);
        }

        if (request.getContacts() != null) {
            List<Contact> contacts = new ArrayList<>();

            if (request.getContacts().getPhone() != null) {
                Contact phoneContact = new Contact();
                phoneContact.setContactType("PHONE");
                phoneContact.setContactValue(request.getContacts().getPhone());
                phoneContact.setHotel(hotel);
                contacts.add(phoneContact);
            }

            if (request.getContacts().getEmail() != null) {
                Contact emailContact = new Contact();
                emailContact.setContactType("EMAIL");
                emailContact.setContactValue(request.getContacts().getEmail());
                emailContact.setHotel(hotel);
                contacts.add(emailContact);
            }

            hotel.setContacts(contacts);
        }
        if (request.getArrivalTime() != null) {
            ArrivalTime arrivalTime = new ArrivalTime();

            if (request.getArrivalTime().getCheckIn() != null) {
                arrivalTime.setCheckIn(parseTime(request.getArrivalTime().getCheckIn()));
            }

            if (request.getArrivalTime().getCheckOut() != null) {
                arrivalTime.setCheckOut(parseTime(request.getArrivalTime().getCheckOut()));
            }

            arrivalTime.setHotel(hotel);
            hotel.setArrivalTime(arrivalTime);
        }

        return hotel;
    }

    private LocalTime parseTime(String timeString) {
        try {
            return LocalTime.parse(timeString, TIME_FORMATTER);
        } catch (Exception e) {
            return LocalTime.parse(timeString);
        }
    }
}