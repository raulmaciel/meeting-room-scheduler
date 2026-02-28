package dev.raul.meeting_room_scheduler.exception;

public class DuplicateMeetingRoomNameException extends RuntimeException {
    public DuplicateMeetingRoomNameException(String message) {
        super(message);
    }
}
