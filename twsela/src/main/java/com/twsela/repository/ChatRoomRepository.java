package com.twsela.repository;

import com.twsela.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByShipmentId(Long shipmentId);

    List<ChatRoom> findByParticipantsContaining(String userId);

    List<ChatRoom> findByShipmentIdAndRoomType(Long shipmentId, ChatRoom.RoomType roomType);

    List<ChatRoom> findByShipmentIdAndStatus(Long shipmentId, ChatRoom.RoomStatus status);
}
