package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.exception.DuplicateMeetingRoomNameException;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.MeetingRoomRepository;
import org.springframework.stereotype.Service;

@Service
public class MeetingRoomService {
    private final MeetingRoomRepository meetingRoomRepository;

    public MeetingRoomService(MeetingRoomRepository meetingRoomRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public MeetingRoom create(CreateMeetingRoomRequest request){
        if (meetingRoomRepository.existsByNameIgnoreCase(request.name())){
            throw new DuplicateMeetingRoomNameException("Meeting room name already exists.");
        }

        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setName(request.name());
        meetingRoom.setAvailable(true);

        return meetingRoomRepository.save(meetingRoom);
    }
}
