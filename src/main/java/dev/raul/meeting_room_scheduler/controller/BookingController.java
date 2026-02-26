package dev.raul.meeting_room_scheduler.controller;

import dev.raul.meeting_room_scheduler.dto.CreateBookingRequest;
import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@Valid @RequestBody CreateBookingRequest request){
        return bookingService.createBooking(request);
    }
}
