package dev.raul.meeting_room_scheduler.repository;

import dev.raul.meeting_room_scheduler.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
