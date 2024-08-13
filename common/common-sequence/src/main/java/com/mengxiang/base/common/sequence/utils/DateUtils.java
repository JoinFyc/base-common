package com.mengxiang.base.common.sequence.utils;

import java.time.*;

/**
 * @author JoinFyc
 * @date 2020/12/14 11:06
 */
public class DateUtils {

    private static final ZoneOffset ZONE_OFF_SET = ZoneOffset.of("+8");

    public static long durationTomorrowMilli() {
        return Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN)).toMillis();
    }

    public static LocalDateTime tomorrow() {
        return LocalDate.now().plusDays(1).atTime(LocalTime.MIN);
    }

    public static long getMill(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZONE_OFF_SET);
    }

    public static long getTomorrowMill() {
        return getMill(tomorrow());
    }

    public static LocalDateTime tomorrow(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().plusDays(1).atTime(LocalTime.MIN);
    }
}
