package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.model.Booking;
import dev.raul.meeting_room_scheduler.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking){
       if(booking.getEndTime().isBefore(booking.getStartTime()) || booking.getEndTime().isEqual(booking.getStartTime())){
           throw new IllegalArgumentException("End time must be after start time.");
       }
       return bookingRepository.save(booking);
    }
}
