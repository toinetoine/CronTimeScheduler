package cron.scheduler.examples;

import cron.scheduler.main.CronScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Every 100ms, write to a log file that rolls every 1 minute on weekdays and every 2 minutes on weekends.
 */
public class RollingLog {
    public static void main(String[] args) throws IOException, InterruptedException {
        final String fileName = args.length > 0 ? args[0] : "test.log";
        // roll every minute on weekdays and 2 minutes on weekends
        CronScheduler cronScheduler =
                new CronScheduler("* * * * Mon-Fri", "*/2 * * * Sat,Sun");
        FileOutputStream fos = null;
        int fileCount = 0;
        long nextRollTime = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z"	);
        while (true) {
            long now = System.currentTimeMillis();
            if (fos == null || now > nextRollTime) {
                fos = new FileOutputStream(new File(fileName + ((fileCount > 0)  ? "." + fileCount : "")));
                fileCount++;
                nextRollTime = cronScheduler.getNextTime(TimeUnit.MILLISECONDS);
            }
            fos.write((simpleDateFormat.format(now) + ": some log statement\n").getBytes());
            Thread.sleep(100);
        }
    }
}
