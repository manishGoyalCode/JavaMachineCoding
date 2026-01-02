package day2p1;

public interface SeatReservationSystem {
    String holdSeat(String seatId, String userId);
    boolean confirmSeat(String holdId);
}
