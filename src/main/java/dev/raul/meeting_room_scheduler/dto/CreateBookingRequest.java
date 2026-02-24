package dev.raul.meeting_room_scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull Long roomId,
        @NotBlank String hostName,
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
){}
