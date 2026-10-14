package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.RentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentReminderLogRepository extends JpaRepository<RentReminderLog, Long> {

    Optional<RentReminderLog> findByNotificationKey(String notificationKey);

    List<RentReminderLog> findByNotificationMonthOrderByIdAsc(String notificationMonth);
}
