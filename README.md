# Garbage-free time scheduler
A garbage-free time scheduler ("scheduler" meaning tracking when to perform an action, not in threading sense). Simply initialize with cron syntax and poll for the next time according to the schedule.


## CronScheduler
Construct a `scheduler` that will trigger at 12:00am & 12:00pm on weedays and 12:00am on weekends
```java
CronScheduler scheduler = new CronScheduler("0 0,12 * * Mon-Fri", "0 0 * * Sat,Sun");
```

### Usage:
#### getNextTime(java.util.concurrent.TimeUnit outTimeUnit)
*Get next scheduled time after now.*

Returns the time since epoch (in unit `outTimeUnit`) of the next upcoming scheduled time (starting from the current time).
Example:
```java
long nextTriggerTime = scheduler.getNextTime(TimeUnit.MILLISECONDS);
```

#### getNextTime(long startTime, TimeUnit inTimeUnit, TimeUnit outTimeUnit)
*Get next scheduled time after a certain time.*

Returns the time since epoch (in unit `outTimeUnit`) of the next upcoming scheduled time (starting from `startTime`).
Example:
```java
long time = System.getCurrentTimeMillis();
...
long nextTriggerTime = scheduler.getNextTime(time, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
```

 ---

#### Example Useage
```java
  CronPeriodicTracker cronPeriodicTracker = new CronPeriodicTracker("0 * * * Mon-Fri", "0 0,12 * * Sat,Sun");
  long nextScheduledTime = cronPeriodicTracker.getNextTime(TimeUnit.MILLISECONDS);

  [loop] {
    long now = System.getCurrentTimeMillis();
    ...
    if (now >= nextScheduledTime) {
      // perform some action action
      ...
      // get the next scheduled time
      nextScheduledTime = scheduler.getNextTime(now, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
    }
  }
```

More examples available [here](src/cron/scheduler/examples).