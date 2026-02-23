package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingService bookingService;

    @Test
    void shouldThrowExceptionWhenEndTimeIsEqualStartTimeAndNotSave(){
        //Arrange
        MeetingRoom meetingRoom = new MeetingRoom();
        Booking booking = new Booking();
        booking.setRoom(meetingRoom);
        booking.setHostName("Raul");
        booking.setTitle("Reunião");

        LocalDateTime start = LocalDateTime.of(2026, 2, 21, 10, 0);
        booking.setStartTime(start);
        booking.setEndTime(start);

        //Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(booking));
        //Assert

        assertEquals("End time must be after start time.", ex.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRoomHasOverlappingBookingAndNotSave(){
        //Arrange
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(1L);
        meetingRoom.setName("Sala 1");

        Booking booking = new Booking();
        booking.setRoom(meetingRoom);
        booking.setHostName("Raul");
        booking.setTitle("Reunião");

        LocalDateTime start = LocalDateTime.of(2026, 2, 21, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 21, 11, 0);
        booking.setStartTime(start);
        booking.setEndTime(end);

        when(bookingRepository.existsOverlappingBooking(meetingRoom.getId(), start, end)).thenReturn(true);
        //Act+Assert

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.createBooking(booking));
        assertEquals("Room is not available for this time slot.", ex.getMessage());

        verify(bookingRepository, never()).save(any());
    }


    @Test
    void shouldSaveWhenNoOverlappingExists(){
        //Arrange
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(1L);
        meetingRoom.setName("Sala A");

        Booking booking = new Booking();
        booking.setRoom(meetingRoom);
        booking.setHostName("Raul");
        booking.setTitle("Kickoff Meeting");

        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        booking.setStartTime(start);
        booking.setEndTime(end);
        //Act+Assert
        when(bookingRepository.existsOverlappingBooking(meetingRoom.getId(), start, end)).thenReturn(false);
        when(bookingRepository.save(booking)).thenReturn(booking);
        Booking saved = bookingService.createBooking(booking);

        assertNotNull(saved);
        verify(bookingRepository, times(1)).save(booking);
    }
}
