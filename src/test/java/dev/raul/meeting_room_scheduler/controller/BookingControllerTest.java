package dev.raul.meeting_room_scheduler.controller;

import dev.raul.meeting_room_scheduler.dto.CreateBookingRequest;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
import dev.raul.meeting_room_scheduler.exception.RoomUnavailableException;
import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.model.BookingStatus;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(ApiExceptionHandler.class)
public class BookingControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BookingService bookingService;

    @Test
    void shouldReturn201WhenBookingCreated() throws Exception{
        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(1L, "Raul", "Reunião", null, start, end);

        MeetingRoom room = new MeetingRoom();

        Booking saved = new Booking();
        saved.setId(100L);
        saved.setRoom(room);
        saved.setHostName("Raul");
        saved.setTitle("Reunião");
        saved.setStartTime(start);
        saved.setEndTime(end);
        saved.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingService.createBooking(request)).thenReturn(saved);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldReturn404WhenRoomNotFound() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                999L, "Raul", "Reunião", null, start, end
        );

        when(bookingService.createBooking(request)).thenThrow(new RoomNotFoundException("Meeting room not found."));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenRoomUnavailable() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 2, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 23, 11, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                1L, "Raul", "Reunião", null, start, end
        );

        when(bookingService.createBooking(request)).thenThrow(new RoomUnavailableException("Room is not available for this time slot."));

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
