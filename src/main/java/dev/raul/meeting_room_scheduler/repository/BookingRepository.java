package dev.raul.meeting_room_scheduler.repository;

import dev.raul.meeting_room_scheduler.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.bookingStatus = dev.raul.meeting_room_scheduler.model.BookingStatus.CONFIRMED
          AND (:start < b.endTime AND :end > b.startTime)
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
