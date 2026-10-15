package com.app.rentmanagement.demo.util;



import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    /**
     * Date on which the rent for rentMonth falls due: the joining day-of-month in the month
     * the rent is collected (rentMonth + monthOffset), clamped to that month's length.
     * A renter who joined on the 31st is due on 28/29 Feb, 31 Mar, 30 Apr, ...
     * Without a joining date the rent is due on the 1st of the collection month.
     */
    public static LocalDate rentDueDate(LocalDate joiningDate, YearMonth rentMonth, int monthOffset) {
        YearMonth collectionMonth = rentMonth.plusMonths(monthOffset);
        if (joiningDate == null) {
            return collectionMonth.atDay(1);
        }
        return collectionMonth.atDay(Math.min(joiningDate.getDayOfMonth(), collectionMonth.lengthOfMonth()));
    }
}
