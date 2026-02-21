package dev.raul.meeting_room_scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String hostName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_room_id", nullable = false)
    private MeetingRoom room;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt = null;
    private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

    public Booking(Long id, String hostName, MeetingRoom room, String title, String description, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt, LocalDateTime updatedAt, BookingStatus bookingStatus) {
        this.id = id;
        this.hostName = hostName;
        this.room = room;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookingStatus = bookingStatus;
    }
}
