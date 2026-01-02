package day2p1;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Reservation {
    String userId;
    Instant expiryTime;
    String holdId;
    boolean isConfirmed;
    public Reservation(String userId, Instant expiryTime) {
        this.userId = userId;
        this.expiryTime = expiryTime;
        this.holdId = UUID.randomUUID().toString().substring(0, 8);
        this.isConfirmed = false;
    }
}
public class SeatReservationSystemImpl implements SeatReservationSystem {
    Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    Map<String,Reservation> holdReservations = new ConcurrentHashMap<>();
    @Override
    public synchronized boolean confirmSeat(String holdId) {
        Reservation reservation = holdReservations.get(holdId);
        if (reservation != null && reservation.isConfirmed) {
            return false;
        }
        if (reservation == null || reservation.expiryTime.isBefore(Instant.now())) {
            return false;
        } else {
            reservation.isConfirmed = true;
            return  true;
        }
    }

    @Override
    public synchronized String holdSeat(String seatId, String userId) {
        Reservation reservation = reservations.get(seatId);
        if(reservation == null || reservation.expiryTime.isBefore(Instant.now())){
            Reservation newReservation = new Reservation(userId, Instant.now().plusMillis(3000));
            reservations.put(seatId, newReservation);
            holdReservations.put(newReservation.holdId, newReservation);
            return newReservation.holdId;
        }
        else {
            return null;
        }
    }
}
