package ru.practicum.test_task.service;

import ru.practicum.test_task.dto.request.CreateHotelRequest;
import ru.practicum.test_task.dto.response.HotelDetailedDto;
import ru.practicum.test_task.dto.response.HotelSummaryDto;

import java.util.List;
import java.util.Map;

public interface HotelService {

    List<HotelSummaryDto> getAllHotels();

    HotelDetailedDto getHotelById(Long id);

    List<HotelSummaryDto> searchHotels(String name, String brand, String city, String county, List<String> amenities);

    HotelSummaryDto createHotel(CreateHotelRequest request);

    void addAmenitiesToHotel(Long hotelId, List<String> amenities);

    Map<String, Long> getHistogram(String param);
}
