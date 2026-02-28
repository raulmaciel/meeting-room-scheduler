package dev.raul.meeting_room_scheduler.controller;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.service.MeetingRoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/room")
public class MeetingRoomController {
    private final MeetingRoomService meetingRoomService;

    public MeetingRoomController(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingRoom create(@Valid @RequestBody CreateMeetingRoomRequest request){
        return meetingRoomService.create(request);
    }
}
