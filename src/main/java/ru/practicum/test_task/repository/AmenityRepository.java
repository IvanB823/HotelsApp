package ru.practicum.test_task.repository;

import ru.practicum.test_task.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Optional<Amenity> findByName(String name);

    @Query("SELECT a FROM Amenity a " +
            "JOIN a.hotels h " +
            "WHERE h.id = :hotelId")
    List<Amenity> findByHotelId(@Param("hotelId") Long hotelId);
}
