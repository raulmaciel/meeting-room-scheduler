package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateBookingRequest;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
import dev.raul.meeting_room_scheduler.exception.RoomUnavailableException;
import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.model.BookingStatus;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.BookingRepository;
import dev.raul.meeting_room_scheduler.repository.MeetingRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, MeetingRoomRepository meetingRoomRepository) {
        this.bookingRepository = bookingRepository;
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public Booking createBooking(CreateBookingRequest request){
       if(request.endTime().isBefore(request.startTime()) || request.endTime().isEqual(request.startTime())){
           throw new IllegalArgumentException("End time must be after start time.");
       }

        MeetingRoom room = meetingRoomRepository.findById(request.roomId()).orElseThrow(() -> new RoomNotFoundException("Meeting room not found."));

        boolean hasConflict = bookingRepository.existsOverlappingBooking(room.getId(), request.startTime(), request.endTime());

        if (hasConflict){
            throw new RoomUnavailableException("Room is not available for this time slot.");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setHostName(request.hostName());
        booking.setTitle(request.title());
        booking.setDescription(request.description());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }
}
