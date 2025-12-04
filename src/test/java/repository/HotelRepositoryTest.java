package repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.test_task.HotelsApp;
import ru.practicum.test_task.model.*;
import ru.practicum.test_task.repository.AmenityRepository;
import ru.practicum.test_task.repository.HotelRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = HotelsApp.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    private Hotel hotel1;
    private Hotel hotel2;
    private Hotel hotel3;
    private Amenity wifi;
    private Amenity pool;
    private Amenity parking;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        amenityRepository.deleteAll();

        wifi = new Amenity();
        wifi.setName("Free WiFi");
        amenityRepository.save(wifi);

        pool = new Amenity();
        pool.setName("Swimming Pool");
        amenityRepository.save(pool);

        parking = new Amenity();
        parking.setName("Parking");
        amenityRepository.save(parking);

        hotel1 = createHotel("Grand Hotel Moscow", "Hilton", "Luxury hotel in city center",
                "Moscow", "Lenina", "10", "Moscow Oblast", "101000",
                List.of(wifi, pool));

        hotel2 = createHotel("Radisson Royal Hotel", "Radisson", "Hotel with river view",
                "Saint Petersburg", "Nevsky Prospekt", "25", "Leningrad Oblast", "191186",
                List.of(wifi));

        hotel3 = createHotel("Moscow Marriott", "Marriott", null,
                "Moscow", "Tverskaya", "15", "Moscow Oblast", "125009",
                new ArrayList<>());
    }

    private Hotel createHotel(String name, String brand, String description,
                              String city, String street, String houseNumber,
                              String county, String postCode, List<Amenity> amenities) {

        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setBrand(brand);
        hotel.setDescription(description);
        hotel.setAmenities(amenities);
        hotel.setContacts(new ArrayList<>());

        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setCounty(county);
        address.setPostCode(postCode);
        address.setHouseNumber(houseNumber);
        address.setHotel(hotel);
        hotel.setAddress(address);

        if (name.equals("Grand Hotel Moscow")) {
            Contact contact = new Contact();
            contact.setContactType("PHONE");
            contact.setContactValue("+79991234567");
            contact.setHotel(hotel);
            hotel.getContacts().add(contact);

            ArrivalTime arrivalTime = new ArrivalTime();
            arrivalTime.setCheckIn(LocalTime.of(14, 0));
            arrivalTime.setCheckOut(LocalTime.of(12, 0));
            arrivalTime.setHotel(hotel);
            hotel.setArrivalTime(arrivalTime);
        }

        return hotelRepository.save(hotel);
    }

    @Test
    void contextLoads() {
        assertThat(hotelRepository).isNotNull();
        assertThat(amenityRepository).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();

        assertThat(hotels).hasSize(3);
        assertThat(hotels).extracting(Hotel::getName)
                .containsExactlyInAnyOrder(
                        "Grand Hotel Moscow",
                        "Radisson Royal Hotel",
                        "Moscow Marriott"
                );
    }

    @Test
    void findById_ShouldReturnHotel() {
        Optional<Hotel> foundHotel = hotelRepository.findById(hotel1.getId());

        assertThat(foundHotel).isPresent();
        assertThat(foundHotel.get().getName()).isEqualTo("Grand Hotel Moscow");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<Hotel> foundHotel = hotelRepository.findById(999L);

        assertThat(foundHotel).isEmpty();
    }

    @Test
    void save_ShouldPersistNewHotel() {
        Hotel newHotel = new Hotel();
        newHotel.setName("New Test Hotel");
        newHotel.setBrand("Test Brand");

        Address address = new Address();
        address.setStreet("Test Street");
        address.setCity("Test City");
        address.setCounty("Test County");
        address.setPostCode("123456");
        address.setHouseNumber("1");
        address.setHotel(newHotel);
        newHotel.setAddress(address);

        newHotel.setAmenities(new ArrayList<>());
        newHotel.setContacts(new ArrayList<>());

        Hotel savedHotel = hotelRepository.save(newHotel);

        assertThat(savedHotel.getId()).isNotNull();
        assertThat(savedHotel.getName()).isEqualTo("New Test Hotel");

        Optional<Hotel> retrievedHotel = hotelRepository.findById(savedHotel.getId());
        assertThat(retrievedHotel).isPresent();
        assertThat(retrievedHotel.get().getName()).isEqualTo("New Test Hotel");
    }

    @Test
    void deleteById_ShouldRemoveHotel() {
        hotelRepository.deleteById(hotel1.getId());

        Optional<Hotel> deletedHotel = hotelRepository.findById(hotel1.getId());
        assertThat(deletedHotel).isEmpty();
    }

    @Test
    void findByIdWithDetails_ShouldReturnHotelWithAddressAndArrivalTime() {
        Optional<Hotel> foundHotel = hotelRepository.findByIdWithDetails(hotel1.getId());

        assertThat(foundHotel).isPresent();
        Hotel hotel = foundHotel.get();
        assertThat(hotel.getAddress()).isNotNull();
        assertThat(hotel.getAddress().getCity()).isEqualTo("Moscow");
        assertThat(hotel.getArrivalTime()).isNotNull();
        assertThat(hotel.getArrivalTime().getCheckIn()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    void findByIdWithAmenities_ShouldReturnHotelWithAmenities() {
        Optional<Hotel> foundHotel = hotelRepository.findByIdWithAmenities(hotel1.getId());

        assertThat(foundHotel).isPresent();
        assertThat(foundHotel.get().getAmenities()).hasSize(2);
        assertThat(foundHotel.get().getAmenities())
                .extracting(Amenity::getName)
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool");
    }

    @Test
    void findByIdWithContacts_ShouldReturnHotelWithContacts() {
        Optional<Hotel> foundHotel = hotelRepository.findByIdWithContacts(hotel1.getId());

        assertThat(foundHotel).isPresent();
        assertThat(foundHotel.get().getContacts()).hasSize(1);
        assertThat(foundHotel.get().getContacts().get(0).getContactValue())
                .isEqualTo("+79991234567");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingHotels() {
        List<Hotel> hotels = hotelRepository.findByNameContainingIgnoreCase("hotel");

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(Hotel::getName)
                .containsExactlyInAnyOrder("Grand Hotel Moscow", "Radisson Royal Hotel");
    }

    @Test
    void findByNameContainingIgnoreCase_WithDifferentCase_ShouldWork() {
        List<Hotel> hotels = hotelRepository.findByNameContainingIgnoreCase("HOTEL");

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findByNameContainingIgnoreCase_WithPartialName_ShouldReturnHotel() {
        List<Hotel> hotels = hotelRepository.findByNameContainingIgnoreCase("Grand");

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getName()).isEqualTo("Grand Hotel Moscow");
    }

    @Test
    void findByBrand_ShouldReturnHotelsWithBrand() {
        List<Hotel> hotels = hotelRepository.findByBrand("Hilton");

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getName()).isEqualTo("Grand Hotel Moscow");
    }

    @Test
    void findByCity_ShouldReturnHotelsInCity() {
        List<Hotel> hotels = hotelRepository.findByCity("Moscow");

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(h -> h.getAddress().getCity())
                .containsOnly("Moscow");
    }

    @Test
    void findByCity_WithCaseInsensitive_ShouldWork() {
        List<Hotel> hotels = hotelRepository.findByCity("MOSCOW");

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findByCounty_ShouldReturnHotelsInCounty() {
        List<Hotel> hotels = hotelRepository.findByCounty("Moscow Oblast");

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(h -> h.getAddress().getCounty())
                .containsOnly("Moscow Oblast");
    }

    @Test
    void findByAmenityName_ShouldReturnHotelsWithAmenity() {
        List<Hotel> hotels = hotelRepository.findByAmenityName("Free WiFi");

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(Hotel::getName)
                .containsExactlyInAnyOrder("Grand Hotel Moscow", "Radisson Royal Hotel");
    }

    @Test
    void findByAmenityName_WithDifferentCase_ShouldWork() {
        List<Hotel> hotels = hotelRepository.findByAmenityName("free wifi");

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findBySearchCriteria_WithNameOnly_ShouldReturnMatchingHotels() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                "Hotel", null, null, null, null);

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findBySearchCriteria_WithCityOnly_ShouldReturnHotelsInCity() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                null, null, "Moscow", null, null);

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findBySearchCriteria_WithBrandAndCity_ShouldReturnFilteredHotels() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                null, "Marriott", "Moscow", null, null);

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getName()).isEqualTo("Moscow Marriott");
    }

    @Test
    void findBySearchCriteria_WithAmenityName_ShouldReturnHotelsWithAmenity() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                null, null, null, null, "Swimming Pool");

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getName()).isEqualTo("Grand Hotel Moscow");
    }

    @Test
    void findBySearchCriteria_WithAllParametersNull_ShouldReturnAllHotels() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                null, null, null, null, null);

        assertThat(hotels).hasSize(3);
    }

    @Test
    void findBySearchCriteria_WithAllParameters_ShouldReturnFilteredHotel() {
        List<Hotel> hotels = hotelRepository.findBySearchCriteria(
                "Grand Hotel Moscow", "Hilton", "Moscow", "Moscow Oblast", "Free WiFi");

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getName()).isEqualTo("Grand Hotel Moscow");
    }

    @Test
    void getHistogramByBrand_ShouldReturnBrandCounts() {
        List<Object[]> histogram = hotelRepository.getHistogramByBrand();

        assertThat(histogram).hasSize(3);

        boolean hiltonFound = false, radissonFound = false, marriottFound = false;
        for (Object[] row : histogram) {
            String brand = (String) row[0];
            Long count = (Long) row[1];
            if ("Hilton".equals(brand)) hiltonFound = true;
            if ("Radisson".equals(brand)) radissonFound = true;
            if ("Marriott".equals(brand)) marriottFound = true;
        }

        assertThat(hiltonFound).isTrue();
        assertThat(radissonFound).isTrue();
        assertThat(marriottFound).isTrue();
    }

    @Test
    void getHistogramByCity_ShouldReturnCityCounts() {
        List<Object[]> histogram = hotelRepository.getHistogramByCity();

        assertThat(histogram).hasSize(2);

        boolean moscowFound = false, stPetersburgFound = false;
        for (Object[] row : histogram) {
            String city = (String) row[0];
            Long count = (Long) row[1];
            if ("Moscow".equals(city)) moscowFound = true;
            if ("Saint Petersburg".equals(city)) stPetersburgFound = true;
        }

        assertThat(moscowFound).isTrue();
        assertThat(stPetersburgFound).isTrue();
    }

    @Test
    void getHistogramByCounty_ShouldReturnCountyCounts() {
        List<Object[]> histogram = hotelRepository.getHistogramByCounty();

        assertThat(histogram).hasSize(2);
    }

    @Test
    void getHistogramByAmenities_ShouldReturnAmenityCounts() {
        List<Object[]> histogram = hotelRepository.getHistogramByAmenities();

        assertThat(histogram).hasSize(3);

        boolean wifiFound = false, poolFound = false, parkingFound = false;
        for (Object[] row : histogram) {
            String amenity = (String) row[0];
            if ("Free WiFi".equals(amenity)) wifiFound = true;
            if ("Swimming Pool".equals(amenity)) poolFound = true;
            if ("Parking".equals(amenity)) parkingFound = true;
        }

        assertThat(wifiFound).isTrue();
        assertThat(poolFound).isTrue();
        assertThat(parkingFound).isTrue();
    }

    @Test
    void amenityRepository_FindByName_ShouldReturnAmenity() {
        Optional<Amenity> foundAmenity = amenityRepository.findByName("Free WiFi");

        assertThat(foundAmenity).isPresent();
        assertThat(foundAmenity.get().getName()).isEqualTo("Free WiFi");
    }

    @Test
    void amenityRepository_FindByHotelId_ShouldReturnAmenitiesForHotel() {
        List<Amenity> amenities = amenityRepository.findByHotelId(hotel1.getId());

        assertThat(amenities).hasSize(2);
        assertThat(amenities).extracting(Amenity::getName)
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool");
    }

    @Test
    void amenityRepository_SaveAndFindAll_ShouldWork() {
        Amenity newAmenity = new Amenity();
        newAmenity.setName("Spa Center");

        Amenity savedAmenity = amenityRepository.save(newAmenity);
        List<Amenity> allAmenities = amenityRepository.findAll();

        assertThat(savedAmenity.getId()).isNotNull();
        assertThat(allAmenities).hasSize(4);
        assertThat(allAmenities).extracting(Amenity::getName)
                .contains("Spa Center");
    }
}