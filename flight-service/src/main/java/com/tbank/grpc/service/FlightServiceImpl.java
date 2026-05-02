package com.tbank.grpc.service;


import com.tbank.grpc.*;
import com.tbank.grpc.exception.AlreadyExistsException;
import com.tbank.grpc.exception.ResourceExhaustedException;
import com.tbank.grpc.exception.ResourceNotFoundException;
import com.tbank.model.entity.Flight;
import com.tbank.model.entity.SeatReservation;
import com.tbank.repository.FlightRepository;
import com.tbank.repository.SeatReservationRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class FlightServiceImpl extends FlightServiceGrpc.FlightServiceImplBase {

    private final FlightRepository flightRepository;
    private final SeatReservationRepository seatReservationRepository;

    @Override
    public void searchFlights(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        String origin = request.getOrigin();
        String destination = request.getDestination();
        Instant date = null;

        if (request.hasDate() && !request.getDate().isEmpty()) {
            date = LocalDate.parse(request.getDate()).atStartOfDay().toInstant(ZoneOffset.UTC);
        }

        List<Flight> flights = flightRepository.searchFlights(origin, destination, date);

        SearchResponse response = SearchResponse.newBuilder()
                .addAllFlights(flights.stream().map(this::toFlightData).toList())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getFlight(GetFlightRequest request, StreamObserver<FlightData> responseObserver) {
        UUID flightId = UUID.fromString(request.getFlightId());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + request.getFlightId()));

        responseObserver.onNext(toFlightData(flight));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void reserveSeats(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        UUID flightId = UUID.fromString(request.getFlightId());
        UUID bookingId = UUID.fromString(request.getBookingId());
        int seatCount = request.getSeatCount();

        if (seatCount <= 0) {
            throw new IllegalArgumentException("Seat count must be positive");
        }

        Optional<SeatReservation> existing = seatReservationRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            throw new AlreadyExistsException("Reservation already exists for booking: " + bookingId);
        }

        Flight flight = flightRepository.findByIdWithLock(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + request.getFlightId()));

        if (flight.getAvailableSeats() < seatCount) {
            throw new ResourceExhaustedException(
                    "Not enough seats. Available: " + flight.getAvailableSeats() + ", Requested: " + seatCount
            );
        }

        int newAvailableSeats = flight.getAvailableSeats() - seatCount;
        flightRepository.updateAvailableSeats(flightId, newAvailableSeats);

        SeatReservation reservation = seatReservationRepository.createReservation(
                flightId, bookingId, seatCount, 10
        );

        ReserveResponse response = ReserveResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Seats reserved successfully")
                .setReservationId(reservation.getId().toString())
                .setSeatCount(seatCount)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();    }

    @Override
    @Transactional
    public void releaseReservation(ReleaseRequest request, StreamObserver<ReleaseResponse> responseObserver) {
        UUID bookingId = UUID.fromString(request.getBookingId());

        SeatReservation reservation = seatReservationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No active reservation found for booking: " + bookingId));

        if (!"ACTIVE".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Reservation is not active. Status: " + reservation.getStatus());
        }

        Flight flight = flightRepository.findById(reservation.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found for reservation"));

        int newAvailableSeats = flight.getAvailableSeats() + reservation.getSeatCount();
        flightRepository.updateAvailableSeats(reservation.getFlightId(), newAvailableSeats);

        seatReservationRepository.releaseByBookingId(bookingId);

        ReleaseResponse response = ReleaseResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Reservation released successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private FlightData toFlightData(Flight flight) {
        return FlightData.newBuilder()
                .setId(flight.getId().toString())
                .setFlightNumber(flight.getFlightNumber())
                .setAirline(flight.getAirline())
                .setOriginIata(flight.getOriginIata())
                .setDestinationIata(flight.getDestinationIata())
                .setDepartureTime(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(flight.getDepartureTime().getEpochSecond())
                        .setNanos(flight.getDepartureTime().getNano())
                        .build())
                .setArrivalTime(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(flight.getArrivalTime().getEpochSecond())
                        .setNanos(flight.getArrivalTime().getNano())
                        .build())
                .setTotalSeats(flight.getTotalSeats())
                .setAvailableSeats(flight.getAvailableSeats())
                .setPrice(flight.getPrice().doubleValue())
                .setStatus(FlightStatus.valueOf(flight.getStatus()))
                .build();
    }
}
