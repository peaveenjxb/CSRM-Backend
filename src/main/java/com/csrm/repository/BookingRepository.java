package com.csrm.repository;

import com.csrm.dto.Enums.BookingStatus;
import com.csrm.model.Booking;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Overlap detection: two bookings clash when each starts before the other ends
    @Query("select count(b) > 0 from Booking b where b.resource.id = :rid and b.status = :st "
            + "and b.startTime < :end and b.endTime > :start and b.id <> :excludeId")
    boolean existsOverlap(@Param("rid") Long rid, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                          @Param("excludeId") Long excludeId, @Param("st") BookingStatus st);

    @Query("select b from Booking b where b.startTime < :to and b.endTime > :from order by b.startTime")
    List<Booking> searchAll(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select b from Booking b where b.resource.id = :rid and b.startTime < :to and b.endTime > :from order by b.startTime")
    List<Booking> searchByResource(@Param("rid") Long rid, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Booking> findByUser_IdOrderByStartTimeDesc(Long userId);

    @Query("select count(b) from Booking b where b.status = :st and b.endTime > :now")
    long countActive(@Param("st") BookingStatus st, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.status = :st and b.reminderSent = false and b.startTime between :from and :to")
    List<Booking> findReminderDue(@Param("st") BookingStatus st, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
