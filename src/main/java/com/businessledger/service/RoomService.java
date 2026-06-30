package com.businessledger.service;

import com.businessledger.entity.Room;
import com.businessledger.enums.RoomStatus;
import com.businessledger.exception.RoomNotAvailableException;
import com.businessledger.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus(RoomStatus.IDLE);
    }

    public List<Room> getAvailableRooms(LocalDateTime time) {
        return roomRepository.findByStatus(RoomStatus.IDLE);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("包间不存在: " + id));
    }

    @Transactional
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = getRoomById(roomId);
        room.setStatus(status);
        room.setUpdateTime(LocalDateTime.now());
        roomRepository.save(room);
    }

    @Transactional
    public void validateRoomAvailable(Long roomId) {
        Room room = getRoomById(roomId);
        if (room.getStatus() != RoomStatus.IDLE) {
            throw new RoomNotAvailableException(roomId,
                    String.format("包间[%s]当前状态为[%s]，不可用", room.getName(), room.getStatus().getDescription()));
        }
    }
}