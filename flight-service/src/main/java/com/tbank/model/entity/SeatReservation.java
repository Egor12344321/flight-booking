package com.tbank.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SeatReservation {
    private UUID id;
    private UUID flightId;
    private UUID bookingId;
    private int seatCount;
    private String status;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
