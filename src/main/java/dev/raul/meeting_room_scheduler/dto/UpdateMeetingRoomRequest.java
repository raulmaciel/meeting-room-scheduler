package dev.raul.meeting_room_scheduler.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMeetingRoomRequest(@NotBlank String name, boolean available) {
}
