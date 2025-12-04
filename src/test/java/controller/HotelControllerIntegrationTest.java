package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.test_task.HotelsApp;
import ru.practicum.test_task.dto.request.CreateHotelRequest;
import ru.practicum.test_task.dto.request.AddressRequest;
import ru.practicum.test_task.dto.request.ContactRequest;
import ru.practicum.test_task.dto.request.ArrivalTimeRequest;
import ru.practicum.test_task.model.Hotel;
import ru.practicum.test_task.model.Address;
import ru.practicum.test_task.model.Contact;
import ru.practicum.test_task.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HotelsApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HotelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();

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
        testHotel = hotelRepository.save(testHotel);
    }

    @Test
    void getAllHotels_ShouldReturnOkWithHotelList() throws Exception {
        mockMvc.perform(get("/property-view/hotels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testHotel.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Hotel"))
                .andExpect(jsonPath("$[0].description").value("Test description"))
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].phone").value("+375 17 309-80-00"));
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotelDetails() throws Exception {
        Long hotelId = testHotel.getId();

        mockMvc.perform(get("/property-view/hotels/{id}", hotelId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(hotelId))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.brand").value("Hilton"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.contacts").exists())
                .andExpect(jsonPath("$.amenities").isArray());
    }

    @Test
    void getHotelById_WhenHotelNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/property-view/hotels/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createHotel_WithValidRequest_ShouldReturnCreated() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("New Grand Hotel");
        request.setBrand("Marriott");
        request.setDescription("Luxury hotel in city center");

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setHouseNumber("25");
        addressRequest.setStreet("Lenina Street");
        addressRequest.setCity("Moscow");
        addressRequest.setCounty("Russia");
        addressRequest.setPostCode("101000");
        request.setAddress(addressRequest);

        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setPhone("+7 495 123-45-67");
        contactRequest.setEmail("info@grandhotel.com");
        request.setContacts(contactRequest);

        ArrivalTimeRequest arrivalTimeRequest = new ArrivalTimeRequest();
        arrivalTimeRequest.setCheckIn("14:00");
        arrivalTimeRequest.setCheckOut("12:00");
        request.setArrivalTime(arrivalTimeRequest);

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Grand Hotel"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.phone").value("+7 495 123-45-67"));

        List<Hotel> allHotels = hotelRepository.findAll();
        assertThat(allHotels).hasSize(2);
    }

    @Test
    void createHotel_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("");

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.contacts").exists());
    }

    @Test
    void createHotel_WithInvalidAddress_ShouldReturnBadRequest() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("Test Hotel");

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setCity("");
        addressRequest.setStreet("");
        request.setAddress(addressRequest);

        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setPhone("123");
        request.setContacts(contactRequest);

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['address.city']").exists())
                .andExpect(jsonPath("$['address.street']").exists());
    }

    @Test
    void searchHotels_ByCity_ShouldReturnFilteredResults() throws Exception {
        String city = "Minsk";

        mockMvc.perform(get("/property-view/search")
                        .param("city", city))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Hotel"))
                .andExpect(jsonPath("$[0].address").value(org.hamcrest.Matchers.containsString("Minsk")));
    }

    @Test
    void searchHotels_ByName_ShouldReturnFilteredResults() throws Exception {
        String name = "Test";

        mockMvc.perform(get("/property-view/search")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Hotel"));
    }

    @Test
    void searchHotels_ByBrand_ShouldReturnFilteredResults() throws Exception {
        String brand = "Hilton";

        mockMvc.perform(get("/property-view/search")
                        .param("brand", brand))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Hotel"));
    }

    @Test
    void searchHotels_WithMultipleParams_ShouldReturnFilteredResults() throws Exception {
        String city = "Minsk";
        String brand = "Hilton";

        mockMvc.perform(get("/property-view/search")
                        .param("city", city)
                        .param("brand", brand))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Hotel"));
    }

    @Test
    void searchHotels_WithNoResults_ShouldReturnEmptyArray() throws Exception {
        String city = "NonExistentCity";

        mockMvc.perform(get("/property-view/search")
                        .param("city", city))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addAmenitiesToHotel_WithValidData_ShouldReturnCreated() throws Exception {
        Long hotelId = testHotel.getId();
        String amenitiesJson = "[\"Free WiFi\", \"Swimming Pool\", \"Parking\"]";

        mockMvc.perform(post("/property-view/hotels/{id}/amenities", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(amenitiesJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/property-view/hotels/{id}", hotelId))
                .andExpect(jsonPath("$.amenities").isArray())
                .andExpect(jsonPath("$.amenities.length()").value(3));
    }

    @Test
    void addAmenitiesToHotel_WhenHotelNotExists_ShouldReturnNotFound() throws Exception {
        String amenitiesJson = "[\"Free WiFi\"]";

        mockMvc.perform(post("/property-view/hotels/{id}/amenities", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(amenitiesJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void addAmenitiesToHotel_WithEmptyList_ShouldReturnCreated() throws Exception {
        Long hotelId = testHotel.getId();
        String amenitiesJson = "[]";

        mockMvc.perform(post("/property-view/hotels/{id}/amenities", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(amenitiesJson))
                .andExpect(status().isCreated());
    }

    @Test
    void getHistogram_ForCity_ShouldReturnCityCounts() throws Exception {
        Hotel secondHotel = createTestHotel("Second Hotel", "Marriott", "Minsk");
        hotelRepository.save(secondHotel);

        mockMvc.perform(get("/property-view/histogram/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Minsk").value(2));
    }

    @Test
    void getHistogram_ForBrand_ShouldReturnBrandCounts() throws Exception {
        Hotel secondHotel = createTestHotel("Second Hilton", "Hilton", "Moscow");
        hotelRepository.save(secondHotel);

        mockMvc.perform(get("/property-view/histogram/brand"))
                .andDo(result -> {
                    System.out.println("Response status: " + result.getResponse().getStatus());
                    System.out.println("Response content: " + result.getResponse().getContentAsString());
                    if (result.getResolvedException() != null) {
                        result.getResolvedException().printStackTrace();
                    }
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Hilton").value(2));
    }

    @Test
    void getHistogram_ForCounty_ShouldReturnCountyCounts() throws Exception {
        mockMvc.perform(get("/property-view/histogram/county"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Belarus").value(1));
    }

    @Test
    void getHistogram_WithInvalidParameter_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/property-view/histogram/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private Hotel createTestHotel(String name, String brand, String city) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setBrand(brand);
        hotel.setDescription("Test hotel");
        hotel.setAmenities(new ArrayList<>());
        hotel.setContacts(new ArrayList<>());

        Address address = new Address();
        address.setCity(city);
        address.setStreet("Test Street");
        address.setCounty("Test");
        address.setPostCode("000000");
        address.setHotel(hotel);
        hotel.setAddress(address);

        Contact contact = new Contact();
        contact.setContactType("PHONE");
        contact.setContactValue("+111 111 11-11");
        contact.setHotel(hotel);

        if (hotel.getContacts() == null) {
            hotel.setContacts(new ArrayList<>());
        }
        hotel.getContacts().add(contact);

        return hotel;
    }
}