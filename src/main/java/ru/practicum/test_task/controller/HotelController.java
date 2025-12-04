package ru.practicum.test_task.controller;

import ru.practicum.test_task.dto.request.CreateHotelRequest;
import ru.practicum.test_task.dto.response.HotelDetailedDto;
import ru.practicum.test_task.dto.response.HotelSummaryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.test_task.service.HotelService;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/property-view")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Управление отелями")
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "Получение списка всех отелей")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = HotelSummaryDto.class))
                    )
            )
    })
    @GetMapping("/hotels")
    public ResponseEntity<List<HotelSummaryDto>> getAllHotels() {
        List<HotelSummaryDto> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @Operation(summary = "Получение детальной информации об отеле")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HotelDetailedDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отель с указанным ID не найден"
            )
    })
    @GetMapping("/hotels/{id}")
    public ResponseEntity<HotelDetailedDto> getHotelById(
            @Parameter(description = "ID отеля", required = true)
            @PathVariable Long id) {
        HotelDetailedDto hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }

    @Operation(summary = "Поиск отелей по параметрам")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = HotelSummaryDto.class))
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<HotelSummaryDto>> searchHotels(
            @Parameter(description = "Название отеля ")
            @RequestParam(required = false) String name,

            @Parameter(description = "Бренд отеля")
            @RequestParam(required = false) String brand,

            @Parameter(description = "Город")
            @RequestParam(required = false) String city,

            @Parameter(description = "Страна")
            @RequestParam(required = false) String county,

            @Parameter(description = "Список удобств", schema = @Schema(type = "array", implementation = String.class))
            @RequestParam(required = false) List<String> amenities) {

        List<HotelSummaryDto> hotels = hotelService.searchHotels(name, brand, city, county, amenities);
        return ResponseEntity.ok(hotels);
    }

    @Operation(summary = "Создание нового отеля")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HotelSummaryDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Отель с таким названием уже существует")
    })
    @PostMapping("/hotels")
    public ResponseEntity<HotelSummaryDto> createHotel(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания отеля",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateHotelRequest.class)
                    )
            )
            @Valid @RequestBody CreateHotelRequest request) {

        HotelSummaryDto createdHotel = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHotel);
    }

    @Operation(summary = "Добавление удобств к отелю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "404", description = "Отель с указанным ID не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный список удобств")
    })
    @PostMapping("/hotels/{id}/amenities")
    public ResponseEntity<Void> addAmenitiesToHotel(
            @Parameter(description = "ID отеля", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список удобств для добавления",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = String.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    value = "[\"Wi-Fi\", \"Кондиционер\", \"Мини-бар\"]"
                            )
                    )
            )
            @RequestBody List<String> amenities) {

        hotelService.addAmenitiesToHotel(id, amenities);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получение гистограммы распределения по определённому параметру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400")
    })
    @GetMapping("/histogram/{param}")
    public ResponseEntity<Map<String, Long>> getHistogram(
            @Parameter(description = "Параметр для построения гистограммы",
                    schema = @Schema(allowableValues = {"city", "county", "brand", "amenities"})
            )
            @PathVariable String param) {
        Map<String, Long> histogram = hotelService.getHistogram(param);
        return ResponseEntity.ok(histogram);
    }
}