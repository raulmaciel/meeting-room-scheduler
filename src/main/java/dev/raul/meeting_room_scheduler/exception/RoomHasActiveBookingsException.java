package dev.raul.meeting_room_scheduler.exception;

public class RoomHasActiveBookingsException extends RuntimeException {
    public RoomHasActiveBookingsException(String message) {
        super(message);
    }
}
