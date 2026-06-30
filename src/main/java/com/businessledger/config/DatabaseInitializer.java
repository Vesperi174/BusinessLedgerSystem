package com.businessledger.config;

import com.businessledger.entity.Room;
import com.businessledger.enums.RoomType;
import com.businessledger.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final RoomRepository roomRepository;

    public DatabaseInitializer(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            log.info("初始化包间数据...");

            List<Room> rooms = List.of(
                    new Room("1号小包", RoomType.SMALL, new BigDecimal("228.00")),
                    new Room("2号中包", RoomType.MEDIUM, new BigDecimal("298.00")),
                    new Room("3号大包", RoomType.LARGE, new BigDecimal("398.00")),
                    new Room("4号中包", RoomType.MEDIUM, new BigDecimal("298.00")),
                    new Room("5号大包", RoomType.LARGE, new BigDecimal("398.00")),
                    new Room("大厅", RoomType.HALL, new BigDecimal("35.00")),
                    new Room("大厅大包", RoomType.HALL_LARGE, new BigDecimal("398.00"))
            );

            roomRepository.saveAll(rooms);
            log.info("包间数据初始化完成，共 {} 条", rooms.size());
        } else {
            log.info("包间数据已存在，共 {} 条，跳过初始化", roomRepository.count());
        }
    }
}