package ru.practicum.test_task.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "house_number")
    private String houseNumber;

    private String street;
    private String city;
    private String county;

    @Column(name = "post_code")
    private String postCode;

    @OneToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
