package cron.scheduler.main;

enum CronTimePortion {
    MINUTE(0), HOUR_OF_DAY(1), DAY_OF_MONTH(2), MONTH(3), DAY_OF_WEEK(4);
    private final int value;

    CronTimePortion(int value) {
        this.value = value;
    }

    protected int getValue() {
        return value;
    }
}