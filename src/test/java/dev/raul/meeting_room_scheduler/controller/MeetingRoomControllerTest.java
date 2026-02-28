package dev.raul.meeting_room_scheduler.controller;

import dev.raul.meeting_room_scheduler.dto.CreateMeetingRoomRequest;
import dev.raul.meeting_room_scheduler.exception.DuplicateMeetingRoomNameException;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(post("/api/v1/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest(" ");

        mockMvc.perform(post("/api/v1/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenNameAlreadyExists() throws Exception {
        CreateMeetingRoomRequest request = new CreateMeetingRoomRequest("Sala 1");

        when(meetingRoomService.create(request))
                .thenThrow(new DuplicateMeetingRoomNameException("Meeting room name already exists."));

        mockMvc.perform(post("/api/v1/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
