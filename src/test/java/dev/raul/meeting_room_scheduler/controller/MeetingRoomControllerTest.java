package dev.raul.meeting_room_scheduler.controller;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.dto.UpdateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.exception.DuplicateMeetingRoomNameException;
import dev.raul.meeting_room_scheduler.exception.RoomNotFoundException;
import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import dev.raul.meeting_room_scheduler.service.MeetingRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MeetingRoomController.class)
@Import(ApiExceptionHandler.class)
public class MeetingRoomControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MeetingRoomService meetingRoomService;

    @Test
    void shouldReturn201WhenRoomCreated() throws Exception {
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest("Sala 1");

        MeetingRoom created = new MeetingRoom();
        created.setId(1L);
        created.setName("Sala 1");
        created.setAvailable(true);

        when(meetingRoomService.create(request)).thenReturn(created);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest(" ");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenNameAlreadyExists() throws Exception {
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest("Sala 1");

        when(meetingRoomService.create(request))
                .thenThrow(new DuplicateMeetingRoomNameException("Meeting room name already exists."));

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200AndListRooms() throws Exception{
        MeetingRoom r1 = new MeetingRoom();
        r1.setId(1L);
        r1.setName("Sala 1");
        r1.setAvailable(true);

        when(meetingRoomService.listAll()).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200AndRoom() throws Exception{
        MeetingRoom r1 = new MeetingRoom();
        r1.setId(1L);
        r1.setName("Sala 1");
        r1.setAvailable(true);

        when(meetingRoomService.getById(1L)).thenReturn(r1);

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404NotExists() throws Exception {

        when(meetingRoomService.getById(999L)).thenThrow(new RoomNotFoundException("Meeting room not found."));

        mockMvc.perform(get("/api/v1/rooms/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenRoomUpdated() throws Exception {
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala Nova", false);

        MeetingRoom updated = new MeetingRoom();
        updated.setId(1L);
        updated.setName("Sala Nova");
        updated.setAvailable(false);

        when(meetingRoomService.update(1L, request)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());

    }

    @Test
    void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest(" ", true);

        mockMvc.perform(put("/api/v1/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdateRoomNotFound() throws Exception {
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala X", true);

        when(meetingRoomService.update(999L, request)).thenThrow(new RoomNotFoundException("Meeting room not found."));

        mockMvc.perform(put("/api/v1/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenUpdateRoomNameAlreadyExists() throws Exception {
        UpdateMeetingRoomRequest request = new UpdateMeetingRoomRequest("Sala 2", true);

        when(meetingRoomService.update(1L, request)).thenThrow(new DuplicateMeetingRoomNameException("Meeting room name already exists."));

        mockMvc.perform(put("/api/v1/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

    }

}
