package app.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeConvert {
  public static Date atStartOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
    return localDateTimeToDate(startOfDay);
  }

  public static Date atEndOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
    return localDateTimeToDate(endOfDay);
  }

  public static LocalDateTime dateToLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static Date localDateTimeToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}