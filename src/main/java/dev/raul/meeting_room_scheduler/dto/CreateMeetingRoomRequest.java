package dev.raul.meeting_room_scheduler.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMeetingRoomRequest(
        @NotBlank String name
) {
}
