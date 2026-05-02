package com.tbank.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    private UUID id;
    private String flightNumber;
    private String airline;
    private String originIata;
    private String destinationIata;
    private Instant departureTime;
    private Instant arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private BigDecimal price;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
