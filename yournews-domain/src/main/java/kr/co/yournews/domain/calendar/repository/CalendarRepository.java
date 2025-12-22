package kr.co.yournews.domain.calendar.repository;

import kr.co.yournews.domain.calendar.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface CalendarRepository extends JpaRepository<Calendar, Long>, CustomCalendarRepository {

    @Query("select c from calendar c where year(c.startAt) in :years")
    List<Calendar> findAllByYears(@Param("years") Set<Integer> years);

    @Query("select c from calendar c where c.startAt <= :endOfMonth and c.endAt >= :startOfMonth")
    List<Calendar> findAllByMonth(@Param("startOfMonth") LocalDate startOfMonth,
                                  @Param("endOfMonth") LocalDate endOfMonth);

    @Modifying
    @Query("delete from calendar c where c.id in :ids")
    void deleteAllByIdInBatch(@Param("ids") List<Long> ids);

    @Modifying
    @Query("delete from calendar c where year(c.startAt) <= :cutoffYear")
    void deleteAllOlderThanYear(@Param("cutoffYear") int cutoffYear);
}
