package cron.scheduler.main;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A schedule initialized from a cron expression
 */
class Schedule {
    private static final Map<String, Integer> DAYS_OF_WEEK_VALUES = new HashMap<String, Integer>();
    static {
        // monday
        DAYS_OF_WEEK_VALUES.put("mon", Calendar.MONDAY);
        DAYS_OF_WEEK_VALUES.put("Mon", Calendar.MONDAY);
        DAYS_OF_WEEK_VALUES.put("monday", Calendar.MONDAY);
        DAYS_OF_WEEK_VALUES.put("Monday", Calendar.MONDAY);
        // tuesday
        DAYS_OF_WEEK_VALUES.put("tue", Calendar.TUESDAY);
        DAYS_OF_WEEK_VALUES.put("Tue", Calendar.TUESDAY);
        DAYS_OF_WEEK_VALUES.put("tuesday", Calendar.TUESDAY);
        DAYS_OF_WEEK_VALUES.put("Tuesday", Calendar.TUESDAY);
        // wednesday
        DAYS_OF_WEEK_VALUES.put("wed", Calendar.WEDNESDAY);
        DAYS_OF_WEEK_VALUES.put("Wed", Calendar.WEDNESDAY);
        DAYS_OF_WEEK_VALUES.put("wednesday", Calendar.WEDNESDAY);
        DAYS_OF_WEEK_VALUES.put("Wednesday", Calendar.WEDNESDAY);
        // thursday
        DAYS_OF_WEEK_VALUES.put("thu", Calendar.THURSDAY);
        DAYS_OF_WEEK_VALUES.put("Thu", Calendar.THURSDAY);
        DAYS_OF_WEEK_VALUES.put("thursday", Calendar.THURSDAY);
        DAYS_OF_WEEK_VALUES.put("Thursday", Calendar.THURSDAY);
        // friday
        DAYS_OF_WEEK_VALUES.put("fri", Calendar.FRIDAY);
        DAYS_OF_WEEK_VALUES.put("Fri", Calendar.FRIDAY);
        DAYS_OF_WEEK_VALUES.put("friday", Calendar.FRIDAY);
        DAYS_OF_WEEK_VALUES.put("Friday", Calendar.FRIDAY);
        // saturday
        DAYS_OF_WEEK_VALUES.put("sat", Calendar.SATURDAY);
        DAYS_OF_WEEK_VALUES.put("Sat", Calendar.SATURDAY);
        DAYS_OF_WEEK_VALUES.put("saturday", Calendar.SATURDAY);
        DAYS_OF_WEEK_VALUES.put("Saturday", Calendar.SATURDAY);
        // sunday
        DAYS_OF_WEEK_VALUES.put("sun", Calendar.SUNDAY);
        DAYS_OF_WEEK_VALUES.put("Sun", Calendar.SUNDAY);
        DAYS_OF_WEEK_VALUES.put("sunday", Calendar.SUNDAY);
        DAYS_OF_WEEK_VALUES.put("Sunday", Calendar.SUNDAY);
    }

    private static final Map<String, Integer> MONTHS_VALUES = new HashMap<String, Integer>();
    static {
        // january
        MONTHS_VALUES.put("jan", Calendar.JANUARY);
        MONTHS_VALUES.put("january", Calendar.JANUARY);
        // february
        MONTHS_VALUES.put("feb", Calendar.FEBRUARY);
        MONTHS_VALUES.put("february", Calendar.FEBRUARY);
        // march
        MONTHS_VALUES.put("mar", Calendar.MARCH);
        MONTHS_VALUES.put("march", Calendar.MARCH);
        // april
        MONTHS_VALUES.put("apr", Calendar.APRIL);
        MONTHS_VALUES.put("april", Calendar.APRIL);
        // may
        MONTHS_VALUES.put("may", Calendar.MAY);
        // june
        MONTHS_VALUES.put("jun", Calendar.JUNE);
        MONTHS_VALUES.put("june", Calendar.JUNE);
        // july
        MONTHS_VALUES.put("jul", Calendar.JULY);
        MONTHS_VALUES.put("july", Calendar.JULY);
        // august
        MONTHS_VALUES.put("aug", Calendar.AUGUST);
        MONTHS_VALUES.put("august", Calendar.AUGUST);
        // september
        MONTHS_VALUES.put("september", Calendar.SEPTEMBER);
        MONTHS_VALUES.put("sep", Calendar.SEPTEMBER);
        // october
        MONTHS_VALUES.put("october", Calendar.OCTOBER);
        MONTHS_VALUES.put("oct", Calendar.OCTOBER);
        // november
        MONTHS_VALUES.put("november", Calendar.NOVEMBER);
        MONTHS_VALUES.put("nov", Calendar.NOVEMBER);
        // december
        MONTHS_VALUES.put("december", Calendar.DECEMBER);
        MONTHS_VALUES.put("dec", Calendar.DECEMBER);
    }

    Schedule(String scheduleString) {
        // expecting 'min(s) hour(s) day(s) month(s) weekday(s)', ex: '0 0 * * *' (roll every day at midnight)
        final CronTimePortion[] cronExpressionOrder = new CronTimePortion[] {
                CronTimePortion.MINUTE,
                CronTimePortion.HOUR_OF_DAY,
                CronTimePortion.DAY_OF_MONTH,
                CronTimePortion.MONTH,
                CronTimePortion.DAY_OF_WEEK };

        String[] dateTimePortions = scheduleString.split("\\s+", 0);
        if (dateTimePortions.length == 5) {
            for (int pI = 0; pI < dateTimePortions.length; pI++) {
                final CronTimePortion currentTimePortion = cronExpressionOrder[pI];
                String[] valuesInPortion = dateTimePortions[pI].split(",", 0);
                //noinspection ForLoopReplaceableByForEach
                for (int vI = 0; vI < valuesInPortion.length; vI++) {
                    boolean rangeCharExists = valuesInPortion[vI].contains("-");
                    boolean stepCharExists = valuesInPortion[vI].contains("/");
                    if (!rangeCharExists && !stepCharExists) {
                        // neither a range or step
                        if (valuesInPortion[vI].equals("*")) {
                            for (int value = 0; value <= Long.SIZE; value++) {
                                // set value in currentTimePortion's mask at position {value}
                                this.set(currentTimePortion, value);
                            }
                        } else {
                            int value = parseValue(valuesInPortion[vI], currentTimePortion);
                            this.set(currentTimePortion, value);
                        }
                    } else {
                        if (rangeCharExists && stepCharExists) {
                            String errorMessage = "Can't have both a step ('*/d') and a range ('a-b') in '" +
                                    valuesInPortion[vI] + "' in the schedule '" + scheduleString + "'";
                            System.err.println(errorMessage);
                            throw new InvalidParameterException(errorMessage);
                        } else if (rangeCharExists) { // range character only
                            String[] rangeParts = valuesInPortion[vI].split("-", -1);
                            if (rangeParts.length == 2) {
                                int lowValue = parseValue(rangeParts[0], currentTimePortion);
                                int highValue = parseValue(rangeParts[1], currentTimePortion);

                                // Base case, set all values from lowValue-to-highValue (inclusive)
                                if (lowValue <= highValue) {
                                    for (int value = lowValue; value <= highValue; value++) {
                                        this.set(currentTimePortion, value);
                                    }
                                } else {
                                    String errorMessage = "The high value of a range must be greater than or equal " +
                                            "to the low value. This is NOT the case in the range '" +
                                            valuesInPortion[vI] + "' in the schedule '" + scheduleString + "'";
                                    System.err.println(errorMessage);
                                    throw new InvalidParameterException(errorMessage);
                                }
                            } else {
                                String errorMessage = "More than one '-' range character in '" + valuesInPortion[vI] +
                                        "' in the schedule '" + scheduleString + "'";
                                System.err.println(errorMessage);
                                throw new InvalidParameterException(errorMessage);
                            }
                        } else { // step character only
                            String[] stepParts = valuesInPortion[vI].split("/", -1);
                            if (stepParts.length == 2) {
                                if (stepParts[0].equals("*")) {
                                    if (stepParts[1].matches("^[0-9]*$")) {
                                        int denominator =  Integer.parseInt(stepParts[1]);
                                        for (int value = 0; value <= Long.SIZE; value++) {
                                            if (value % denominator == 0) {
                                                // set value in currentTimePortion's mask at position {value}
                                                this.set(currentTimePortion, value);
                                            }
                                        }
                                    } else {
                                        String errorMessage = "The denominator of '" + valuesInPortion[vI] +
                                                "' must be an integer in the schedule '" + scheduleString + "'";
                                        System.err.println(errorMessage);
                                        throw new InvalidParameterException(errorMessage);
                                    }
                                } else {
                                    String errorMessage = "The numerator of '" + valuesInPortion[vI] +
                                            "' must equal '*' in the schedule '" + scheduleString + "'";
                                    System.err.println(errorMessage);
                                    throw new InvalidParameterException(errorMessage);
                                }
                            } else {
                                String errorMessage = "More than one '/' step character in '" + valuesInPortion[vI] +
                                        "' in the schedule '" + scheduleString + "'";
                                System.err.println(errorMessage);
                                throw new InvalidParameterException(errorMessage);
                            }
                        }
                    }
                }
            }
        } else {
            String errorMessage = "The following cron schedule cannot be read: '" + scheduleString + "'. Rollover " +
                    "schedules must be in the following format (with 5 time portions): " +
                    "'min(s) hour(s) day(s) month(s) weekday(s)'. Each portion must be present and there can be NO " +
                    "other spaces in the cron expression other than those that separate the time portions. " +
                    "Example schedule for rolling on 10:00:00am and 3:00:00pm on weekdays: '0 0 10,15 * * M-F'";
            System.err.println(errorMessage);
            throw new InvalidParameterException(errorMessage);
        }
    }

    private int parseValue(String rawValue, CronTimePortion timePortion) {
        int value;
        switch (timePortion) {
            case MINUTE:
                try {
                    value = Integer.parseInt(rawValue);
                } catch(NumberFormatException nfe) {
                    String errorMessage = "Unable to parse this minute value as an integer: '" + rawValue +
                            "'. Original Exception: " + nfe.toString();
                    System.err.println(errorMessage);
                    throw new NumberFormatException(errorMessage);
                }
                if (value < 0 || value > 59) {
                    String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as minute(s) " +
                            "(must be greater than or equal to 0 AND less than or equal to 59).";
                    System.err.println(errorMessage);
                    throw new InvalidParameterException(errorMessage);
                }
                break;
            case HOUR_OF_DAY:
                try {
                    value = Integer.parseInt(rawValue);
                    if(value < 0 || value > 23) {
                        String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as hour(s) " +
                                "(must be greater than or equal to 0 AND be less than or equal to 23).";
                        System.err.println(errorMessage);
                        throw new InvalidParameterException(errorMessage);
                    }
                } catch(NumberFormatException nfe) {
                    String errorMessage = "Unable to parse this hour value as an integer: '" + rawValue +
                            "'. Original Exception: " + nfe.getMessage();
                    System.err.println(errorMessage);
                    throw new NumberFormatException(errorMessage);
                }
                break;
            case DAY_OF_MONTH:
                try {
                    value = Integer.parseInt(rawValue);
                    if (value < 1 || value > 31) {
                        String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as day of " +
                                "month (must be greater than or equal to 1 and less than or equal to 31).";
                        System.err.println(errorMessage);
                        throw new InvalidParameterException(errorMessage);
                    }
                } catch(NumberFormatException nfe) {
                    String errorMessage = "Unable to parse this day-of-month value as an integer: '" + rawValue +
                            "'. Original Exception: " + nfe.getMessage();
                    System.err.println(errorMessage);
                    throw new NumberFormatException(errorMessage);
                }
                break;
            case MONTH:
                if (rawValue.matches("^[0-9]*$")) {
                    try {
                        int inputValue = Integer.parseInt(rawValue);
                        if (inputValue < 1 || inputValue > 12) {
                            String errorMessage = "Time schedule: Couldn't parse value '" + rawValue +
                                    "' as a month (must be greater than or equal to 1 and less than or equal to 12).";
                            System.err.println(errorMessage);
                            throw new InvalidParameterException(errorMessage);
                        }
                        value = inputValue - (1 - Calendar.JANUARY);
                    } catch(NumberFormatException nfe) {
                        String errorMessage = "Unable to parse this month value as an integer: '" + rawValue +
                                "'. Original Exception: " + nfe.getMessage();
                        System.err.println(errorMessage);
                        throw new NumberFormatException(errorMessage);
                    }
                } else {
                    Integer monthValue = MONTHS_VALUES.get(rawValue.toLowerCase());
                    if (monthValue == null) {
                        String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as a month.";
                        System.err.println(errorMessage);
                        throw new InvalidParameterException(errorMessage);
                    } else {
                        value = monthValue;
                    }
                }
                break;
            case DAY_OF_WEEK:
                if (rawValue.matches("^[0-9]*$")) {
                    switch (Integer.parseInt(rawValue)) {
                        case 0:
                            value = Calendar.SUNDAY;
                            break;
                        case 1:
                            value = Calendar.MONDAY;
                            break;
                        case 2:
                            value = Calendar.TUESDAY;
                            break;
                        case 3:
                            value = Calendar.WEDNESDAY;
                            break;
                        case 4:
                            value = Calendar.THURSDAY;
                            break;
                        case 5:
                            value = Calendar.FRIDAY;
                            break;
                        case 6:
                            value = Calendar.SATURDAY;
                            break;
                        default:
                            String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as a day of week. " +
                                    "Acceptable days of week values are {0: Sunday, 1: Monday, ....., 6: Saturday}";
                            System.err.println(errorMessage);
                            throw new InvalidParameterException(errorMessage);
                    }
                } else {
                    Integer dayOfWeekValue = DAYS_OF_WEEK_VALUES.get(rawValue.toLowerCase());
                    if (dayOfWeekValue == null) {
                        String errorMessage = "Time schedule: Couldn't parse value '" + rawValue + "' as a day of week.";
                        System.err.println(errorMessage);
                        throw new InvalidParameterException(errorMessage);
                    } else {
                        value = dayOfWeekValue;
                    }
                }
                break;
            default:
                throw new InvalidParameterException("Unhandled time portion for ");
        }

        return value;
    }

    // 0: minute, 1: hour, 2: day of month, 3: month, 4: day of week
    private final long[] timePortionMasks = new long[5];

    boolean get(CronTimePortion timePortion, int offset) {
        return ((timePortionMasks[timePortion.getValue()] >> offset) & 1L) == 1L;
    }

    private void set(CronTimePortion timePortion, int offset) {
        timePortionMasks[timePortion.getValue()] |= (1L << offset);
    }
}
