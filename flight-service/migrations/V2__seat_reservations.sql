CREATE TABLE IF NOT EXISTS seat_reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flight_id UUID NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    booking_id UUID NOT NULL,
    seat_count INTEGER NOT NULL CHECK (seat_count > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CONFIRMED', 'RELEASED', 'EXPIRED')),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT one_reservation_per_booking UNIQUE (booking_id)
);

CREATE INDEX IF NOT EXISTS idx_reservations_flight ON seat_reservations(flight_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_expires ON seat_reservations(expires_at) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_reservations_booking ON seat_reservations(booking_id);

DROP TRIGGER IF EXISTS update_reservations_updated_at ON seat_reservations;
CREATE TRIGGER update_reservations_updated_at
    BEFORE UPDATE ON seat_reservations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();