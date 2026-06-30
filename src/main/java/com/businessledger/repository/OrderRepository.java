package com.businessledger.repository;

import com.businessledger.entity.Order;
import com.businessledger.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByRoomId(Long roomId);

    List<Order> findByRoomIdAndStatus(Long roomId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createTime BETWEEN :start AND :end ORDER BY o.createTime DESC")
    List<Order> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.room.id = :roomId AND o.createTime BETWEEN :start AND :end ORDER BY o.createTime DESC")
    List<Order> findByRoomIdAndDateRange(@Param("roomId") Long roomId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createTime BETWEEN :start AND :end ORDER BY o.createTime DESC")
    List<Order> findByStatusAndDateRange(@Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.room.id = :roomId AND o.status = :status AND o.createTime BETWEEN :start AND :end ORDER BY o.createTime DESC")
    List<Order> findByRoomIdAndStatusAndDateRange(@Param("roomId") Long roomId, @Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}