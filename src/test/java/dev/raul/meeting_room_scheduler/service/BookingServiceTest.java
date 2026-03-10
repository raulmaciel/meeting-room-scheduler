package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateBookingRequest;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
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

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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

    @Test
    void shouldThrowExceptionWhenRoomNotExistsThenNotConsultOverlapAndNotSave(){
        //Arrange

        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                999L,
                "Raul",
                "Reunião",
                null,
                start,
                end
        );

        when(meetingRoomRepository.findById(999L)).thenReturn(Optional.empty());

        //Act + Assert

        RoomNotFoundException ex = assertThrows(RoomNotFoundException.class, () -> bookingService.createBooking(request));
        assertEquals("Meeting room not found.", ex.getMessage());

        verify(meetingRoomRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).existsOverlappingBooking(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldListAllBookingsWhenNoFilters(){
        //Arrange
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(1L);
        meetingRoom.setName("Sala A");

        LocalDateTime startB1 = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime endB1   = LocalDateTime.of(2026, 2, 23, 11, 0);

        LocalDateTime startB2 = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime endB2   = LocalDateTime.of(2026, 2, 23, 12, 0);

        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setRoom(meetingRoom);
        b1.setHostName("Raul");
        b1.setTitle("Reunião 1");
        b1.setStartTime(startB1);
        b1.setEndTime(endB1);

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setRoom(meetingRoom);
        b2.setHostName("Raul");
        b2.setTitle("Reunião 2");
        b2.setStartTime(startB2);
        b2.setEndTime(endB2);

        when(bookingRepository.findAll()).thenReturn(List.of(b1,b2));

        // Act

        List<Booking> bookings = bookingService.listBookings(null, null);

        //Assert
        assertEquals(2, bookings.size());
        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository,never()).findByRoomId(anyLong());
    }

    @Test
    void shouldListBookingsWhenFilteringByRoomId(){
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(1L);
        meetingRoom.setName("Sala A");

        Long id = 1L;

        LocalDateTime startB1 = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime endB1   = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime startB2 = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime endB2   = LocalDateTime.of(2026, 2, 23, 12, 0);


        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setRoom(meetingRoom);
        b1.setHostName("Raul");
        b1.setTitle("Reunião 1");
        b1.setStartTime(startB1);
        b1.setEndTime(endB1);

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setRoom(meetingRoom);
        b2.setHostName("Raul");
        b2.setTitle("Reunião 2");
        b2.setStartTime(startB2);
        b2.setEndTime(endB2);

        when(bookingRepository.findByRoomId(1L)).thenReturn(List.of(b1, b2));

        List<Booking> bookingsFound = bookingService.listBookings(id, null);

        assertNotNull(bookingsFound);
        assertEquals(2, bookingsFound.size());
        assertEquals(1L, bookingsFound.get(0).getId());
        assertEquals(2L, bookingsFound.get(1).getId());

        verify(bookingRepository, times(1)).findByRoomId(1L);
        verify(bookingRepository, never()).findAll();
    }

    @Test
    void shouldListBookingsWhenFilteringByDate(){
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(1L);
        meetingRoom.setName("Sala A");

        LocalDateTime startB1 = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime endB1   = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime startB2 = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime endB2   = LocalDateTime.of(2026, 2, 23, 12, 0);


        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setRoom(meetingRoom);
        b1.setHostName("Raul");
        b1.setTitle("Reunião 1");
        b1.setStartTime(startB1);
        b1.setEndTime(endB1);

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setRoom(meetingRoom);
        b2.setHostName("Raul");
        b2.setTitle("Reunião 2");
        b2.setStartTime(startB2);
        b2.setEndTime(endB2);

        LocalDate date = LocalDate.of(2026, 2, 23);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23,59,59, 999);

        when(bookingRepository.findByStartTimeBetween(dayStart, dayEnd)).thenReturn(List.of(b1,b2));

        List<Booking> bookingsFound = bookingService.listBookings(null, date);

        assertNotNull(bookingsFound);
        assertEquals(2, bookingsFound.size());
        assertEquals(1L, bookingsFound.get(0).getId());
        assertEquals(2L, bookingsFound.get(1).getId());

        verify(bookingRepository, times(1)).findByStartTimeBetween(dayStart, dayEnd);
        verify(bookingRepository, never()).findAll();
    }

    @Test
    void shouldListBookingsWhenFilteringByRoomIdAndDate(){
        Long roomId = 1L;

        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(roomId);
        meetingRoom.setName("Sala A");

        LocalDateTime startB1 = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime endB1   = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime startB2 = LocalDateTime.of(2026, 2, 23, 11, 0);
        LocalDateTime endB2   = LocalDateTime.of(2026, 2, 23, 12, 0);


        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setRoom(meetingRoom);
        b1.setHostName("Raul");
        b1.setTitle("Reunião 1");
        b1.setStartTime(startB1);
        b1.setEndTime(endB1);

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setRoom(meetingRoom);
        b2.setHostName("Raul");
        b2.setTitle("Reunião 2");
        b2.setStartTime(startB2);
        b2.setEndTime(endB2);

        LocalDate date = LocalDate.of(2026, 2, 23);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23,59,59, 999);

        when(bookingRepository.findByRoomIdAndStartTimeBetween(roomId, dayStart, dayEnd)).thenReturn(List.of(b1,b2));

        List<Booking> bookingsFound = bookingService.listBookings(roomId, date);

        assertEquals(2, bookingsFound.size());
        assertEquals(1L, bookingsFound.get(0).getId());
        assertEquals(2L, bookingsFound.get(1).getId());

        verify(bookingRepository, times(1)).findByRoomIdAndStartTimeBetween(roomId, dayStart, dayEnd);
        verify(bookingRepository, never()).findAll();
    }
}
