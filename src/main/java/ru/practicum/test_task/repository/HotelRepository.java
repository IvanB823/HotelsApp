package ru.practicum.test_task.repository;

import ru.practicum.test_task.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h " +
            "LEFT JOIN FETCH h.address " +
            "LEFT JOIN FETCH h.arrivalTime " +
            "WHERE h.id = :id")
    Optional<Hotel> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT h FROM Hotel h " +
            "LEFT JOIN FETCH h.amenities " +
            "WHERE h.id = :id")
    Optional<Hotel> findByIdWithAmenities(@Param("id") Long id);

    @Query("SELECT h FROM Hotel h " +
            "LEFT JOIN FETCH h.contacts " +
            "WHERE h.id = :id")
    Optional<Hotel> findByIdWithContacts(@Param("id") Long id);

    List<Hotel> findByNameContainingIgnoreCase(String name);

    List<Hotel> findByBrand(String brand);

    @Query("SELECT h FROM Hotel h " +
            "JOIN h.address a " +
            "WHERE LOWER(a.city) = LOWER(:city)")
    List<Hotel> findByCity(@Param("city") String city);

    @Query("SELECT h FROM Hotel h " +
            "JOIN h.address a " +
            "WHERE LOWER(a.county) = LOWER(:county)")
    List<Hotel> findByCounty(@Param("county") String county);

    @Query("SELECT DISTINCT h FROM Hotel h " +
            "JOIN h.amenities a " +
            "WHERE LOWER(a.name) = LOWER(:amenityName)")
    List<Hotel> findByAmenityName(@Param("amenityName") String amenityName);

    @Query("SELECT DISTINCT h FROM Hotel h " +
            "LEFT JOIN h.address a " +
            "LEFT JOIN h.amenities am " +
            "WHERE (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:brand IS NULL OR LOWER(h.brand) = LOWER(:brand)) " +
            "AND (:city IS NULL OR LOWER(a.city) = LOWER(:city)) " +
            "AND (:county IS NULL OR LOWER(a.county) = LOWER(:county)) " +
            "AND (:amenityName IS NULL OR am.name = :amenityName)")
    List<Hotel> findBySearchCriteria(@Param("name") String name,
                                     @Param("brand") String brand,
                                     @Param("city") String city,
                                     @Param("county") String county,
                                     @Param("amenityName") String amenityName);

    @Query("SELECT h.brand, COUNT(h) FROM Hotel h WHERE h.brand IS NOT NULL GROUP BY h.brand")
    List<Object[]> getHistogramByBrand();

    @Query("SELECT a.city, COUNT(h) FROM Hotel h JOIN h.address a GROUP BY a.city")
    List<Object[]> getHistogramByCity();

    @Query("SELECT a.county, COUNT(h) FROM Hotel h JOIN h.address a GROUP BY a.county")
    List<Object[]> getHistogramByCounty();

    @Query("SELECT a.name, COUNT(h) FROM Amenity a " +
            "LEFT JOIN a.hotels h " +
            "GROUP BY a.id, a.name " +
            "ORDER BY a.name")
    List<Object[]> getHistogramByAmenities();
}
