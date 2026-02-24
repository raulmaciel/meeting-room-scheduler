package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateBookingRequest;
import dev.raul.meeting_room_scheduler.exception.RoomUnavailableException;
import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.BookingRepository;
import dev.raul.meeting_room_scheduler.repository.MeetingRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    MeetingRoomRepository meetingRoomRepository;

    @InjectMocks
    BookingService bookingService;

    @Test
    void shouldThrowExceptionWhenEndTimeIsEqualStartTimeAndNotSave(){
        //Arrange

        LocalDateTime start = LocalDateTime.of(2026, 2, 21, 10, 0);
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "Raul",
                "Reunião",
                null,
                start,
                start
        );

        //Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(request));
        //Assert

        assertEquals("End time must be after start time.", ex.getMessage());
        verify(bookingRepository, never()).save(any());
        verify(bookingRepository, never()).existsOverlappingBooking(any(), any(), any());
        verify(meetingRoomRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenRoomHasOverlappingBookingAndNotSave(){
        //Arrange
        LocalDateTime start = LocalDateTime.of(2026, 2, 21, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 21, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "Raul",
                "Reunião",
                null,
                start,
                end
        );

        MeetingRoom room = new MeetingRoom();
        room.setId(1L);
        room.setName("Sala 1");

        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(true);
        //Act+Assert

        RoomUnavailableException ex = assertThrows(RoomUnavailableException.class, () -> bookingService.createBooking(request));
        assertEquals("Room is not available for this time slot.", ex.getMessage());

        verify(bookingRepository, never()).save(any());
    }


    @Test
    void shouldSaveWhenNoOverlappingExists(){
        //Arrange
        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "Raul",
                "Reunião",
                null,
                start,
                end
        );

        MeetingRoom room = new MeetingRoom();
        room.setId(1L);
        room.setName("Sala A");

        //Act+Assert
        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(false);


        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        Booking saved = bookingService.createBooking(request);

        assertNotNull(saved);
        assertEquals("Raul", saved.getHostName());
        assertEquals("Reunião", saved.getTitle());
        assertEquals(room, saved.getRoom());
        assertEquals(start, saved.getStartTime());
        assertEquals(end, saved.getEndTime());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }
}
