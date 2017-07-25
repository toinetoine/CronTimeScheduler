package cron.scheduler.main;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * Garbage-free cron scheduler
 */
public class CronScheduler {
    private Schedule[] schedules;
    private final Calendar calendar = Calendar.getInstance();

    public CronScheduler(String... stringSchedules) {
        schedules = new Schedule[stringSchedules.length];
        for (int i = 0; i < stringSchedules.length; i++) {
            schedules[i] = new Schedule(stringSchedules[i]);
        }
    }

    public long getNextTime(TimeUnit outTimeUnit) {
        return getNextTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS, outTimeUnit);
    }

    /**
     * Get the schedule's next occurrence (default search space is 1 year)
     * @return the ms epoch time for the next occurrence or Long.MAX if nothing is found within the next year
     */
    public long getNextTime(long startTime, TimeUnit inTimeUnit, TimeUnit outTimeUnit) {
        long soonestTime = Long.MAX_VALUE;
        startTime = MILLISECONDS.convert(startTime, inTimeUnit);

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < schedules.length; i++) {
            long schedulesSoonestTime = getNextTime(startTime, schedules[i]);
            if (schedulesSoonestTime < soonestTime) {
                soonestTime = schedulesSoonestTime;
            }
        }

        return outTimeUnit.convert(soonestTime, MILLISECONDS);
    }

    /**
     *
     * @return Returns the next occurrence of a time following the schedule within the next year.
     *      Returns Long.MAX_VALUE if no time is found within the next year.
     */
    private long getNextTime(long startTime, Schedule schedule) {
        calendar.setTimeInMillis(startTime);
        calendar.add(Calendar.YEAR, 1);
        long endTime = calendar.getTimeInMillis();

        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 1);

        // while not at the start of the startTime's month, 1 year after startTime...
        while (calendar.getTimeInMillis() < endTime) {
            if (!schedule.get(CronTimePortion.MONTH, calendar.get(Calendar.MONTH))) {
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            } else if (!schedule.get(CronTimePortion.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)) ||
                    !schedule.get(CronTimePortion.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK))) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            } else if (!schedule.get(CronTimePortion.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))) {
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
            } else if (!schedule.get(CronTimePortion.MINUTE, calendar.get(Calendar.MINUTE))) {
                calendar.add(Calendar.MINUTE, 1);
            } else {
                return calendar.getTimeInMillis();
            }
        }

        return Long.MAX_VALUE;
    }
}
