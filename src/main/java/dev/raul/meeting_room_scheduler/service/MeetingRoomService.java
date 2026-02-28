package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.dto.UpdateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.exception.DuplicateMeetingRoomNameException;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.MeetingRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<MeetingRoom> listAll(){
        return meetingRoomRepository.findAll();
    }

    public MeetingRoom getById(Long id){
        return meetingRoomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException("Meeting room not found."));
    }

    public MeetingRoom update(Long id, UpdateMeetingRoomRequest request){

        MeetingRoom existing = meetingRoomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException("Meeting room not found."));

        if (meetingRoomRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)){
            throw new DuplicateMeetingRoomNameException("Meeting room name already exists.");
        }

        existing.setName(request.name());
        existing.setAvailable(request.available());

        return meetingRoomRepository.save(existing);
    }
}
