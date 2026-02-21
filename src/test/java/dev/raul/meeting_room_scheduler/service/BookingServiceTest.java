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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
}
