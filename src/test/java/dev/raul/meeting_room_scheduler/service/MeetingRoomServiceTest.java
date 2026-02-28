package dev.raul.meeting_room_scheduler.service;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.dto.UpdateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.exception.DuplicateMeetingRoomNameException;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.repository.MeetingRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingRoomServiceTest {
    @Mock
    MeetingRoomRepository meetingRoomRepository;

    @InjectMocks
    MeetingRoomService meetingRoomService;

    @Test
    void shouldCreateRoomWhenNameIsUnique(){
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest("Sala 1");

        when(meetingRoomRepository.existsByNameIgnoreCase("Sala 1")).thenReturn(false);
        when(meetingRoomRepository.save(any(MeetingRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        MeetingRoom created = meetingRoomService.create(request);

        assertNotNull(created);
        assertEquals("Sala 1", created.getName());
        verify(meetingRoomRepository, times(1)).save(any(MeetingRoom.class));
    }

    @Test
    void shouldThrowWhenRoomNameAlreadyExistsAndNotSave(){
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest("Sala 1");

        when(meetingRoomRepository.existsByNameIgnoreCase("Sala 1")).thenReturn(true);

        DuplicateMeetingRoomNameException ex = assertThrows(DuplicateMeetingRoomNameException.class, () -> meetingRoomService.create(request));

        assertEquals("Meeting room name already exists.", ex.getMessage());
        verify(meetingRoomRepository, never()).save(any());
    }

    @Test
    void shouldListAllRooms(){
        MeetingRoom r1 = new MeetingRoom();
        r1.setId(1L);
        r1.setName("Sala 1");
        r1.setAvailable(true);

        MeetingRoom r2 = new MeetingRoom();
        r2.setId(2L);
        r2.setName("Sala 2");
        r2.setAvailable(true);

        MeetingRoom r3 = new MeetingRoom();
        r2.setId(3L);
        r2.setName("Sala 3");
        r2.setAvailable(true);

        when(meetingRoomRepository.findAll()).thenReturn(List.of(r1,r2,r3));

        List<MeetingRoom> rooms = meetingRoomService.listAll();

        assertEquals(3, rooms.size());
        verify(meetingRoomRepository, times(1)).findAll();

    }

    @Test
    void shouldGetRoomByIdWhenExists(){
        MeetingRoom room = new MeetingRoom();
        room.setId(1L);
        room.setName("Sala 1");
        room.setAvailable(true);

        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        MeetingRoom found = meetingRoomService.getById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void shouldThrowWhenRoomIdNotFound(){
        when(meetingRoomRepository.findById(999L)).thenReturn(Optional.empty());

        RoomNotFoundException ex = assertThrows(RoomNotFoundException.class, () -> meetingRoomService.getById(999L));

        assertEquals("Meeting room not found.", ex.getMessage());
    }

    @Test
    void shouldUpdateRoomWhenExistsAndNameIsUnique(){
        // Arrange
        Long roomId = 1L;
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala Nova", false);

        MeetingRoom existing = new MeetingRoom();
        existing.setId(1L);
        existing.setName("Sala Antiga");
        existing.setAvailable(true);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.of(existing));
        when(meetingRoomRepository.existsByNameIgnoreCaseAndIdNot("Sala Nova", roomId)).thenReturn(false);
        when(meetingRoomRepository.save(any(MeetingRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        meetingRoomService.update(roomId, request);

        //Assert
        verify(meetingRoomRepository, times(1)).findById(roomId);
        verify(meetingRoomRepository, times(1)).existsByNameIgnoreCaseAndIdNot("Sala Nova", roomId);
        verify(meetingRoomRepository, times(1)).save(any(MeetingRoom.class));
    }

    @Test
    void shouldThrowWhenRoomNotFoundAndNotSave(){
        Long roomId = 999L;
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala X", true);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        RoomNotFoundException ex = assertThrows(RoomNotFoundException.class, () -> meetingRoomService.update(roomId, request));

        assertEquals("Meeting room not found.", ex.getMessage());

        verify(meetingRoomRepository, times(1)).findById(roomId);
        verify(meetingRoomRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
        verify(meetingRoomRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNameAlreadyExistsAndNotSave(){
        Long roomId = 1L;
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala 2", true);

        MeetingRoom existing = new MeetingRoom();
        existing.setId(roomId);
        existing.setName("Sala 1");
        existing.setAvailable(true);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.of(existing));
        when(meetingRoomRepository.existsByNameIgnoreCaseAndIdNot("Sala 2", roomId)).thenReturn(true);

        DuplicateMeetingRoomNameException ex = assertThrows(DuplicateMeetingRoomNameException.class, () -> meetingRoomService.update(roomId, request));

        assertEquals("Meeting room name already exists.", ex.getMessage());

        verify(meetingRoomRepository, times(1)).findById(roomId);
        verify(meetingRoomRepository, times(1)).existsByNameIgnoreCaseAndIdNot("Sala 2", roomId);
        verify(meetingRoomRepository, never()).save(any());
    }

}
