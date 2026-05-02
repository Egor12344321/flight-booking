package com.tbank.repository;

import com.tbank.model.entity.Flight;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FlightRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Flight> findById(UUID id) {
        String sql = "SELECT * FROM flights WHERE id = ?";
        List<Flight> flights = jdbcTemplate.query(sql, flightRowMapper, id);
        return flights.isEmpty() ? Optional.empty() : Optional.of(flights.get(0));
    }

    public List<Flight> searchFlights(String origin, String destination, Instant date) {
        String sql = "SELECT * FROM flights WHERE origin_iata = ? AND destination_iata = ? AND status = 'SCHEDULED'";
        Object[] params;

        if (date != null) {
            sql += " AND DATE(departure_time) = DATE(?)";
            params = new Object[]{origin, destination, java.sql.Timestamp.from(date)};
        } else {
            params = new Object[]{origin, destination};
        }

        return jdbcTemplate.query(sql, flightRowMapper, params);
    }

    public Optional<Flight> findByIdWithLock(UUID id, UUID bookingId) {
        String sql = "SELECT * FROM flights WHERE id = ? FOR UPDATE";
        List<Flight> flights = jdbcTemplate.query(sql, flightRowMapper, id);
        return flights.isEmpty() ? Optional.empty() : Optional.of(flights.get(0));
    }

    public int updateAvailableSeats(UUID flightId, int newAvailableSeats) {
        String sql = "UPDATE flights SET available_seats = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newAvailableSeats, flightId);
    }

    private final RowMapper<Flight> flightRowMapper = (rs, rowNum) -> {
        Flight flight = new Flight();
        flight.setId(UUID.fromString(rs.getString("id")));
        flight.setFlightNumber(rs.getString("flight_number"));
        flight.setAirline(rs.getString("airline"));
        flight.setOriginIata(rs.getString("origin_iata"));
        flight.setDestinationIata(rs.getString("destination_iata"));
        flight.setDepartureTime(rs.getTimestamp("departure_time").toInstant());
        flight.setArrivalTime(rs.getTimestamp("arrival_time").toInstant());
        flight.setTotalSeats(rs.getInt("total_seats"));
        flight.setAvailableSeats(rs.getInt("available_seats"));
        flight.setPrice(rs.getBigDecimal("price"));
        flight.setStatus(rs.getString("status"));
        flight.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        flight.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return flight;
    };

}