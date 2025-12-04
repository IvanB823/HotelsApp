package repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.test_task.HotelsApp;
import ru.practicum.test_task.model.Amenity;
import ru.practicum.test_task.model.Hotel;
import ru.practicum.test_task.model.Address;
import ru.practicum.test_task.repository.AmenityRepository;
import ru.practicum.test_task.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = HotelsApp.class)
@ActiveProfiles("test")
@Transactional
class AmenityRepositoryTest {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Amenity wifi;
    private Amenity pool;
    private Hotel hotel1;
    private Hotel hotel2;

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

        hotel1 = new Hotel();
        hotel1.setName("Hotel 1");
        hotel1.setAmenities(new ArrayList<>(List.of(wifi, pool)));
        hotel1.setContacts(new ArrayList<>());

        Address address1 = new Address();
        address1.setStreet("Street 1");
        address1.setCity("City 1");
        address1.setHotel(hotel1);
        hotel1.setAddress(address1);

        hotelRepository.save(hotel1);

        hotel2 = new Hotel();
        hotel2.setName("Hotel 2");
        hotel2.setAmenities(new ArrayList<>(List.of(wifi)));
        hotel2.setContacts(new ArrayList<>());

        Address address2 = new Address();
        address2.setStreet("Street 2");
        address2.setCity("City 2");
        address2.setHotel(hotel2);
        hotel2.setAddress(address2);

        hotelRepository.save(hotel2);
    }

    @Test
    void findByName_WhenAmenityExists_ShouldReturnAmenity() {
        Optional<Amenity> foundAmenity = amenityRepository.findByName("Free WiFi");

        assertThat(foundAmenity).isPresent();
        assertThat(foundAmenity.get().getName()).isEqualTo("Free WiFi");
        assertThat(foundAmenity.get().getId()).isEqualTo(wifi.getId());
    }

    @Test
    void findByName_WhenAmenityExistsWithDifferentCase_ShouldReturnEmpty() {
        Optional<Amenity> foundAmenity = amenityRepository.findByName("FREE WIFI");
        assertThat(foundAmenity).isEmpty();
    }

    @Test
    void findByName_WhenAmenityDoesNotExist_ShouldReturnEmpty() {
        Optional<Amenity> foundAmenity = amenityRepository.findByName("Non Existing Amenity");
        assertThat(foundAmenity).isEmpty();
    }

    @Test
    void findByName_WhenMultipleAmenitiesExist_ShouldReturnCorrectOne() {
        Amenity wifiPremium = new Amenity();
        wifiPremium.setName("Premium WiFi");
        amenityRepository.save(wifiPremium);

        Optional<Amenity> foundBasicWifi = amenityRepository.findByName("Free WiFi");
        Optional<Amenity> foundPremiumWifi = amenityRepository.findByName("Premium WiFi");

        assertThat(foundBasicWifi).isPresent();
        assertThat(foundBasicWifi.get().getName()).isEqualTo("Free WiFi");

        assertThat(foundPremiumWifi).isPresent();
        assertThat(foundPremiumWifi.get().getName()).isEqualTo("Premium WiFi");
    }

    @Test
    void findByHotelId_WhenHotelHasAmenities_ShouldReturnAllAmenities() {
        List<Amenity> amenities = amenityRepository.findByHotelId(hotel1.getId());

        assertThat(amenities).hasSize(2);
        assertThat(amenities).extracting(Amenity::getName)
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool");
    }

    @Test
    void findByHotelId_WhenHotelHasNoAmenities_ShouldReturnEmptyList() {
        Hotel hotelWithoutAmenities = new Hotel();
        hotelWithoutAmenities.setName("Hotel Without Amenities");
        hotelWithoutAmenities.setAmenities(new ArrayList<>());
        hotelWithoutAmenities.setContacts(new ArrayList<>());

        Address address = new Address();
        address.setStreet("Street 3");
        address.setCity("City 3");
        address.setHotel(hotelWithoutAmenities);
        hotelWithoutAmenities.setAddress(address);

        hotelRepository.save(hotelWithoutAmenities);
        List<Amenity> amenities = amenityRepository.findByHotelId(hotelWithoutAmenities.getId());
        assertThat(amenities).isEmpty();
    }

    @Test
    void findByHotelId_WhenHotelDoesNotExist_ShouldReturnEmptyList() {
        List<Amenity> amenities = amenityRepository.findByHotelId(999L);
        assertThat(amenities).isEmpty();
    }

    @Test
    void findByHotelId_ShouldReturnDistinctAmenities() {
        List<Amenity> amenities = amenityRepository.findByHotelId(hotel1.getId());

        assertThat(amenities).hasSize(2);
        assertThat(amenities).extracting(Amenity::getName)
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool");
    }

    @Test
    void findByHotelId_AfterAddingNewAmenityToHotel_ShouldReturnUpdatedList() {
        Amenity newAmenity = new Amenity();
        newAmenity.setName("Spa");
        amenityRepository.save(newAmenity);

        List<Amenity> hotelAmenities = new ArrayList<>(hotel1.getAmenities());
        hotelAmenities.add(newAmenity);
        hotel1.setAmenities(hotelAmenities);
        hotelRepository.save(hotel1);

        hotelRepository.flush();
        List<Amenity> amenities = amenityRepository.findByHotelId(hotel1.getId());

        assertThat(amenities).hasSize(3);
        assertThat(amenities).extracting(Amenity::getName)
                .containsExactlyInAnyOrder("Free WiFi", "Swimming Pool", "Spa");
    }

    @Test
    void findByHotelId_AfterRemovingAmenityFromHotel_ShouldReturnUpdatedList() {
        List<Amenity> hotelAmenities = new ArrayList<>(hotel1.getAmenities());
        hotelAmenities.remove(pool);
        hotel1.setAmenities(hotelAmenities);
        hotelRepository.save(hotel1);

        hotelRepository.flush();

        List<Amenity> amenities = amenityRepository.findByHotelId(hotel1.getId());
        assertThat(amenities).hasSize(1);
        assertThat(amenities.get(0).getName()).isEqualTo("Free WiFi");
    }

    @Test
    void findByHotelId_ShouldWorkWithMultipleHotelsHavingSameAmenity() {
        List<Amenity> amenitiesForHotel1 = amenityRepository.findByHotelId(hotel1.getId());
        List<Amenity> amenitiesForHotel2 = amenityRepository.findByHotelId(hotel2.getId());

        assertThat(amenitiesForHotel1).hasSize(2);
        assertThat(amenitiesForHotel2).hasSize(1);

        assertThat(amenitiesForHotel1).extracting(Amenity::getName)
                .contains("Free WiFi");
        assertThat(amenitiesForHotel2).extracting(Amenity::getName)
                .contains("Free WiFi");
    }
}