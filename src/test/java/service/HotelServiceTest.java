package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.test_task.HotelsApp;
import ru.practicum.test_task.dto.request.CreateHotelRequest;
import ru.practicum.test_task.dto.request.AddressRequest;
import ru.practicum.test_task.dto.request.ContactRequest;
import ru.practicum.test_task.dto.request.ArrivalTimeRequest;
import ru.practicum.test_task.dto.response.HotelDetailedDto;
import ru.practicum.test_task.dto.response.HotelSummaryDto;
import ru.practicum.test_task.model.*;
import ru.practicum.test_task.repository.HotelRepository;
import ru.practicum.test_task.repository.AmenityRepository;
import ru.practicum.test_task.service.HotelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = HotelsApp.class)
@ActiveProfiles("test")
@Transactional
class HotelServiceTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        amenityRepository.deleteAll();

        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setBrand("Hilton");
        testHotel.setDescription("Test description");

        testHotel.setAmenities(new ArrayList<>());
        Address address = new Address();
        address.setHouseNumber("9");
        address.setStreet("Pobediteley Avenue");
        address.setCity("Minsk");
        address.setCounty("Belarus");
        address.setPostCode("220004");
        address.setHotel(testHotel);
        testHotel.setAddress(address);

        Contact phoneContact = new Contact();
        phoneContact.setContactType("PHONE");
        phoneContact.setContactValue("+375 17 309-80-00");
        phoneContact.setHotel(testHotel);

        Contact emailContact = new Contact();
        emailContact.setContactType("EMAIL");
        emailContact.setContactValue("test@hotel.com");
        emailContact.setHotel(testHotel);

        testHotel.setContacts(List.of(phoneContact, emailContact));

        ArrivalTime arrivalTime = new ArrivalTime();
        arrivalTime.setCheckIn(java.time.LocalTime.of(14, 0));
        arrivalTime.setCheckOut(java.time.LocalTime.of(12, 0));
        arrivalTime.setHotel(testHotel);
        testHotel.setArrivalTime(arrivalTime);

        hotelRepository.save(testHotel);
    }

    @Test
    void getAllHotels_WhenHotelsExist_ShouldReturnListWithCorrectFields() {
        List<HotelSummaryDto> hotels = hotelService.getAllHotels();

        assertThat(hotels).isNotEmpty();
        assertThat(hotels).hasSize(1);

        HotelSummaryDto dto = hotels.get(0);
        assertThat(dto.getId()).isEqualTo(testHotel.getId());
        assertThat(dto.getName()).isEqualTo("Test Hotel");
        assertThat(dto.getDescription()).isEqualTo("Test description");
        assertThat(dto.getAddress()).isEqualTo("9 Pobediteley Avenue, Minsk, Belarus, 220004");
        assertThat(dto.getPhone()).isEqualTo("+375 17 309-80-00");
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotelDetails() {
        Long hotelId = testHotel.getId();

        HotelDetailedDto result = hotelService.getHotelById(hotelId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(hotelId);
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getBrand()).isEqualTo("Hilton");
        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getContacts()).isNotNull();
        assertThat(result.getArrivalTime()).isNotNull();
        assertThat(result.getAmenities()).isEmpty();
    }

    @Test
    void getHotelById_WhenHotelNotExists_ShouldThrowException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> hotelService.getHotelById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Hotel not found");
    }

    @Test
    void createHotel_WithValidRequest_ShouldCreateNewHotel() {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("New Hotel");
        request.setBrand("Marriott");
        request.setDescription("Brand new hotel");

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setHouseNumber("25");
        addressRequest.setStreet("Lenina Street");
        addressRequest.setCity("Moscow");
        addressRequest.setCounty("Russia");
        addressRequest.setPostCode("101000");
        request.setAddress(addressRequest);

        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setPhone("+7 495 123-45-67");
        contactRequest.setEmail("moscow@marriott.com");
        request.setContacts(contactRequest);

        ArrivalTimeRequest arrivalTimeRequest = new ArrivalTimeRequest();
        arrivalTimeRequest.setCheckIn("15:00");
        arrivalTimeRequest.setCheckOut("11:00");
        request.setArrivalTime(arrivalTimeRequest);

        HotelSummaryDto result = hotelService.createHotel(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Hotel");
        assertThat(result.getDescription()).isEqualTo("Brand new hotel");
        assertThat(result.getAddress()).isEqualTo("25 Lenina Street, Moscow, Russia, 101000");
        assertThat(result.getPhone()).isEqualTo("+7 495 123-45-67");

        List<Hotel> allHotels = hotelRepository.findAll();
        assertThat(allHotels).hasSize(2);
    }

    @Test
    void addAmenitiesToHotel_WithNewAmenities_ShouldAddThem() {
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel for Amenities");
        hotel.setBrand("Test");
        hotel.setAmenities(new ArrayList<>());

        Address address = new Address();
        address.setCity("Minsk");
        address.setStreet("Test");
        address.setHotel(hotel);
        hotel.setAddress(address);

        Hotel savedHotel = hotelRepository.save(hotel);
        Long hotelId = savedHotel.getId();

        List<String> amenities = List.of("Free WiFi", "Swimming Pool", "Parking");

        hotelService.addAmenitiesToHotel(hotelId, amenities);

        HotelDetailedDto hotelDetails = hotelService.getHotelById(hotelId);
        assertThat(hotelDetails.getAmenities()).hasSize(3);
        assertThat(hotelDetails.getAmenities())
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool", "Parking");
    }

    @Test
    void addAmenitiesToHotel_WithDuplicateAmenities_ShouldAddOnlyOnce() {
        Hotel freshHotel = new Hotel();
        freshHotel.setName("Fresh Hotel");
        freshHotel.setBrand("Test");
        freshHotel.setAmenities(new ArrayList<>());

        Address address = new Address();
        address.setCity("Minsk");
        address.setStreet("Test");
        address.setHotel(freshHotel);
        freshHotel.setAddress(address);

        Hotel savedHotel = hotelRepository.save(freshHotel);
        Long hotelId = savedHotel.getId();

        hotelService.addAmenitiesToHotel(hotelId, List.of("Free WiFi", "Pool"));

        hotelService.addAmenitiesToHotel(hotelId, List.of("Free WiFi", "Pool", "Parking"));

        HotelDetailedDto hotelDetails = hotelService.getHotelById(hotelId);
        assertThat(hotelDetails.getAmenities()).hasSize(3);
        assertThat(hotelDetails.getAmenities())
                .containsExactlyInAnyOrder("Free WiFi", "Pool", "Parking");
    }

    @Test
    void searchHotels_ByCity_ShouldReturnFilteredResults() {
        Hotel moscowHotel = new Hotel();
        moscowHotel.setName("Moscow Hotel");
        moscowHotel.setBrand("Marriott");
        moscowHotel.setDescription("Hotel in Moscow");

        Address moscowAddress = new Address();
        moscowAddress.setCity("Moscow");
        moscowAddress.setStreet("Tverskaya");
        moscowAddress.setHouseNumber("1");
        moscowAddress.setCounty("Russia");
        moscowAddress.setPostCode("101000");
        moscowAddress.setHotel(moscowHotel);
        moscowHotel.setAddress(moscowAddress);

        Contact moscowContact = new Contact();
        moscowContact.setContactType("PHONE");
        moscowContact.setContactValue("+7 495 999-99-99");
        moscowContact.setHotel(moscowHotel);
        moscowHotel.setContacts(List.of(moscowContact));

        hotelRepository.save(moscowHotel);
        List<HotelSummaryDto> minskHotels = hotelService.searchHotels(null, null, "Minsk", null, null);
        List<HotelSummaryDto> moscowHotels = hotelService.searchHotels(null, null, "Moscow", null, null);

        assertThat(minskHotels).hasSize(1);
        assertThat(minskHotels.get(0).getName()).isEqualTo("Test Hotel");
        assertThat(minskHotels.get(0).getAddress()).contains("Minsk");

        assertThat(moscowHotels).hasSize(1);
        assertThat(moscowHotels.get(0).getName()).isEqualTo("Moscow Hotel");
        assertThat(moscowHotels.get(0).getAddress()).contains("Moscow");
    }

    @Test
    void searchHotels_ByNamePartialMatch_ShouldReturnResults() {
        List<HotelSummaryDto> results = hotelService.searchHotels("Test", null, null, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Hotel");
        assertThat(results.get(0).getDescription()).isEqualTo("Test description");
    }

    @Test
    void searchHotels_ByBrand_ShouldReturnResults() {
        List<HotelSummaryDto> results = hotelService.searchHotels(null, "Hilton", null, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Hotel");
    }

    @Test
    void getHistogram_ForBrand_ShouldReturnBrandCounts() {
        Hotel secondHilton = new Hotel();
        secondHilton.setName("Second Hilton");
        secondHilton.setBrand("Hilton");
        secondHilton.setDescription("Another Hilton");

        Address address = new Address();
        address.setCity("Minsk");
        address.setStreet("Test Street");
        address.setHouseNumber("10");
        address.setCounty("Belarus");
        address.setPostCode("220100");
        address.setHotel(secondHilton);
        secondHilton.setAddress(address);

        Contact contact = new Contact();
        contact.setContactType("PHONE");
        contact.setContactValue("+375 17 111-11-11");
        contact.setHotel(secondHilton);
        secondHilton.setContacts(List.of(contact));

        hotelRepository.save(secondHilton);

        Map<String, Long> histogram = hotelService.getHistogram("brand");

        assertThat(histogram).isNotEmpty();
        assertThat(histogram.get("Hilton")).isEqualTo(2L);
    }

    @Test
    void getHistogram_ForCity_ShouldReturnCityCounts() {
        Map<String, Long> histogram = hotelService.getHistogram("city");

        assertThat(histogram).isNotEmpty();
        assertThat(histogram.get("Minsk")).isEqualTo(1L);
    }

    @Test
    void getHistogram_ForAmenities_ShouldReturnAmenityCounts() {
        Hotel hotel1 = createHotelWithInitializedCollections("First Hotel", "Hilton", "Minsk");
        Hotel hotel2 = createHotelWithInitializedCollections("Second Hotel", "Marriott", "Moscow");

        Hotel savedHotel1 = hotelRepository.save(hotel1);
        Hotel savedHotel2 = hotelRepository.save(hotel2);

        hotelService.addAmenitiesToHotel(savedHotel1.getId(), List.of("Free WiFi", "Pool"));
        hotelService.addAmenitiesToHotel(savedHotel2.getId(), List.of("Free WiFi", "Parking"));

        Map<String, Long> histogram = hotelService.getHistogram("amenities");

        assertThat(histogram).isNotEmpty();
        assertThat(histogram.get("Free WiFi")).isEqualTo(2L);
        assertThat(histogram.get("Pool")).isEqualTo(1L);
        assertThat(histogram.get("Parking")).isEqualTo(1L);
    }

    private Hotel createHotelWithInitializedCollections(String name, String brand, String city) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setBrand(brand);
        hotel.setDescription("Test hotel in " + city);
        hotel.setAmenities(new ArrayList<>());
        hotel.setContacts(new ArrayList<>());

        Address address = new Address();
        address.setCity(city);
        address.setStreet("Test Street");
        address.setHouseNumber("1");
        address.setCounty("Test County");
        address.setPostCode("000000");
        address.setHotel(hotel);
        hotel.setAddress(address);

        Contact contact = new Contact();
        contact.setContactType("PHONE");
        contact.setContactValue("+111 111 11-11");
        contact.setHotel(hotel);
        hotel.getContacts().add(contact);

        return hotel;
    }

    @Test
    void getHistogram_WithInvalidParameter_ShouldThrowException() {
        assertThatThrownBy(() -> hotelService.getHistogram("invalid_param"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported histogram parameter");
    }
}