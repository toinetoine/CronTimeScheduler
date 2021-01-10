import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import cron.scheduler.main.CronScheduler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CronSchedulerTest {

    private Calendar dateIterator;

    @Before
    public void beforeTest() {
        dateIterator = Calendar.getInstance();
    }

    @Test
    public void simpleTest() throws Exception {
        CronScheduler scheduler = new CronScheduler("0 0 * * *");

        Calendar nextTrigger = Calendar.getInstance();
        nextTrigger.setTimeInMillis(scheduler.getNextTime(TimeUnit.MILLISECONDS));

        Assert.assertEquals(0, nextTrigger.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(0, nextTrigger.get(Calendar.MINUTE));
        dateIterator.add(Calendar.DAY_OF_MONTH, 1); // the triggered datetime should be the next day
        Assert.assertEquals(dateIterator.get(Calendar.DAY_OF_MONTH), nextTrigger.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(dateIterator.get(Calendar.MONTH), nextTrigger.get(Calendar.MONTH));
        Assert.assertEquals(dateIterator.get(Calendar.YEAR), nextTrigger.get(Calendar.YEAR));
        Assert.assertEquals(dateIterator.get(Calendar.DAY_OF_WEEK), nextTrigger.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testRange() throws Exception {
        Calendar nextTrigger = Calendar.getInstance(); // reused

        // on-the-hour every weekday from 12:00am to 12:00pm (inclusive)
        CronScheduler ts = new CronScheduler("0 0-12 * * Mon-Fri");

        // over the next 7 days, 1 hour at a time
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 7);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        while (dateIterator.getTimeInMillis() < endDate.getTimeInMillis()) {
            nextTrigger.setTimeInMillis(ts.getNextTime(dateIterator.getTimeInMillis(), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS));

            int tempsHour = dateIterator.get(Calendar.HOUR_OF_DAY);
            Assert.assertEquals(0, nextTrigger.get(Calendar.MINUTE));
            // if after 12:00pm on Friday to Sunday at 11:59pm then next trigger date will be Monday morning at 12:00am
            if (dateIterator.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || dateIterator.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                    (dateIterator.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && dateIterator.get(Calendar.HOUR_OF_DAY) >= 12)) {
                Assert.assertEquals(Calendar.MONDAY, nextTrigger.get(Calendar.DAY_OF_WEEK));
                Assert.assertEquals(0, nextTrigger.get(Calendar.HOUR_OF_DAY));
            } else {
                if(tempsHour < 12) { // on weekday before 12:00pm (so next time will be on-the-next-hour)
                    Assert.assertEquals(tempsHour + 1, nextTrigger.get(Calendar.HOUR_OF_DAY));
                    Assert.assertEquals(dateIterator.get(Calendar.DAY_OF_MONTH), nextTrigger.get(Calendar.DAY_OF_MONTH));
                } else { // on weekday after 12:00pm (so next time will be the next day at midnight on-the-hour
                    Assert.assertEquals(0, nextTrigger.get(Calendar.HOUR_OF_DAY));
                    Calendar nextDayMidnight = Calendar.getInstance();
                    nextDayMidnight.setTimeInMillis(dateIterator.getTimeInMillis());
                    nextDayMidnight.add(Calendar.DAY_OF_MONTH, 1);
                    nextDayMidnight.set(Calendar.HOUR_OF_DAY, 0);
                    nextDayMidnight.set(Calendar.MINUTE, 0);
                    nextDayMidnight.set(Calendar.SECOND, 0);
                    nextDayMidnight.set(Calendar.MILLISECOND, 0);

                    Assert.assertEquals(nextDayMidnight.getTimeInMillis(), nextTrigger.getTimeInMillis());
                }
            }

            dateIterator.add(Calendar.HOUR_OF_DAY, 1);
        }
    }

    @Test
    public void testStep() throws Exception {
        Calendar nextTrigger = Calendar.getInstance(); // reused, iterates through the next 7 days
        Calendar nextFifteenMinuteMark = Calendar.getInstance(); // reused, marks next time with minutes divisible by 15
        nextFifteenMinuteMark.setTimeInMillis(dateIterator.getTimeInMillis());

        CronScheduler ts = new CronScheduler("*/15 * * * *"); // every 15 minutes

        // over the next 7 days, 1 minute at a time
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 7);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        while (dateIterator.getTimeInMillis() < endDate.getTimeInMillis()) {
            nextTrigger.setTimeInMillis(ts.getNextTime(dateIterator.getTimeInMillis(), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS));

            if(dateIterator.getTimeInMillis() >= nextFifteenMinuteMark.getTimeInMillis()) {
                nextFifteenMinuteMark.setTimeInMillis(dateIterator.getTimeInMillis());
                do {
                    nextFifteenMinuteMark.add(Calendar.MINUTE, 1);
                } while (nextFifteenMinuteMark.get(Calendar.MINUTE) % 15 != 0);
            }

            nextFifteenMinuteMark.set(Calendar.SECOND, 0);
            nextFifteenMinuteMark.set(Calendar.MILLISECOND, 0);

            Assert.assertEquals(nextFifteenMinuteMark.getTimeInMillis(), nextTrigger.getTimeInMillis());
            dateIterator.add(Calendar.MINUTE, 1);
        }
    }

    @Test
    public void testMultipleSchedules() throws Exception {
        dateIterator.set(Calendar.SECOND, 0);
        dateIterator.set(Calendar.MILLISECOND, 0);

        String[] schedules = new String[] {
                "51 13 * * Mon", // Monday at 1:51pm {One}
                "52 14  * * Tue", // Tuesday at 2:52pm {Two}
                "53 15 * * Wed" // Wednesday at 3:53pm {Three}
        };

        CronScheduler ts = new CronScheduler(schedules);

        // Iterate over the  year, 1 hour at a time
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 1);
        while (dateIterator.getTimeInMillis() < endDate.getTimeInMillis()) {
            Calendar temp = Calendar.getInstance();
            temp.setTimeInMillis(dateIterator.getTimeInMillis());

            temp.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            temp.set(Calendar.HOUR_OF_DAY, 13);
            temp.set(Calendar.MINUTE, 51);
            long tsOneTime = temp.getTimeInMillis();

            temp.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            temp.set(Calendar.HOUR_OF_DAY, 14);
            temp.set(Calendar.MINUTE, 52);
            long tsTwoTime = temp.getTimeInMillis();

            temp.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            temp.set(Calendar.HOUR_OF_DAY, 15);
            temp.set(Calendar.MINUTE, 53);
            long tsThreeTime = temp.getTimeInMillis();

            temp.add(Calendar.WEEK_OF_YEAR, 1);
            temp.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            temp.set(Calendar.HOUR_OF_DAY, 13);
            temp.set(Calendar.MINUTE, 51);
            long nextTsOneTime = temp.getTimeInMillis();

            long currentTime = dateIterator.getTimeInMillis();
            long currentNextTriggerTime = ts.getNextTime(currentTime, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);

            if (currentTime < tsOneTime) {
                Assert.assertEquals(tsOneTime, currentNextTriggerTime);
            } else if (currentTime < tsTwoTime) {
                Assert.assertEquals(tsTwoTime, currentNextTriggerTime);
            } else if (currentTime < tsThreeTime) {
                Assert.assertEquals(tsThreeTime, currentNextTriggerTime);
            } else {
                Assert.assertEquals(nextTsOneTime, currentNextTriggerTime);
            }

            dateIterator.add(Calendar.HOUR_OF_DAY, 1);
        }
        ts.getNextTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule for the 10th day of every other month (Jan, Mar, May, Jul, ...)
     * Test by going through 24 months (from the start of this year)
     */
    @Test
    public void testSpecificMonths() throws Exception {
        dateIterator.set(Calendar.SECOND, 0);
        dateIterator.set(Calendar.MILLISECOND, 0);
        dateIterator.set(Calendar.MONTH, Calendar.JANUARY);
        dateIterator.set(Calendar.DAY_OF_MONTH, 1);

        CronScheduler ts = new CronScheduler("0 0 10 */2 *"); // the 10th day of every other month

        long lastNextTriggerTime = 0;
        for (int monthsInAdvance = 0; monthsInAdvance < 24; monthsInAdvance++) {
            long thisNextTriggerTime = ts.getNextTime(dateIterator.getTimeInMillis(), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
            Calendar temp = Calendar.getInstance();
            temp.setTimeInMillis(thisNextTriggerTime);
            Assert.assertEquals(10, temp.get(Calendar.DAY_OF_MONTH));
            Assert.assertTrue(temp.get(Calendar.MONTH) % 2 == 0);
            if(monthsInAdvance % 2 == 0) {
                lastNextTriggerTime = thisNextTriggerTime;
            } else {
                Assert.assertEquals(lastNextTriggerTime, thisNextTriggerTime);
            }
        }
    }
}
