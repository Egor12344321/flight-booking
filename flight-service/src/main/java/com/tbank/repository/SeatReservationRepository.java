package com.tbank.repository;

import com.tbank.model.entity.SeatReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeatReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public SeatReservation createReservation(UUID flightId, UUID bookingId, int seatCount, int ttlMinutes) {
        String sql = "INSERT INTO seat_reservations (id, flight_id, booking_id, seat_count, status, expires_at) " +
                "VALUES (gen_random_uuid(), ?, ?, ?, 'ACTIVE', NOW() + INTERVAL '" + ttlMinutes + " minutes') RETURNING id";

        UUID reservationId = jdbcTemplate.queryForObject(sql, UUID.class, flightId, bookingId, seatCount);

        return findById(reservationId).orElseThrow();
    }

    public Optional<SeatReservation> findById(UUID id) {
        String sql = "SELECT * FROM seat_reservations WHERE id = ?";
        List<SeatReservation> reservations = jdbcTemplate.query(sql, reservationRowMapper, id);
        return reservations.isEmpty() ? Optional.empty() : Optional.of(reservations.get(0));
    }

    public Optional<SeatReservation> findByBookingId(UUID bookingId) {
        String sql = "SELECT * FROM seat_reservations WHERE booking_id = ?";
        List<SeatReservation> reservations = jdbcTemplate.query(sql, reservationRowMapper, bookingId);
        return reservations.isEmpty() ? Optional.empty() : Optional.of(reservations.get(0));
    }

    public int updateStatus(UUID reservationId, String status) {
        String sql = "UPDATE seat_reservations SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, reservationId);
    }

    public int releaseByBookingId(UUID bookingId) {
        String sql = "UPDATE seat_reservations SET status = 'RELEASED' WHERE booking_id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.update(sql, bookingId);
    }

    public int deleteExpired() {
        String sql = "UPDATE seat_reservations SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < NOW()";
        return jdbcTemplate.update(sql);
    }

    private final RowMapper<SeatReservation> reservationRowMapper = (rs, rowNum) -> {
        SeatReservation reservation = new SeatReservation();
        reservation.setId(UUID.fromString(rs.getString("id")));
        reservation.setFlightId(UUID.fromString(rs.getString("flight_id")));
        reservation.setBookingId(UUID.fromString(rs.getString("booking_id")));
        reservation.setSeatCount(rs.getInt("seat_count"));
        reservation.setStatus(rs.getString("status"));
        if (rs.getTimestamp("expires_at") != null) {
            reservation.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
        }
        reservation.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        reservation.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return reservation;
    };
}