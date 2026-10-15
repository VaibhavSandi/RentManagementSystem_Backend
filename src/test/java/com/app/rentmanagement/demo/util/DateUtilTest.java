package com.app.rentmanagement.demo.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    private static final LocalDate JOINED_31_JAN = LocalDate.of(2026, 1, 31);

    @Test
    void dueOnJoiningDayOfCollectionMonth() {
        LocalDate joined = LocalDate.of(2026, 1, 15);
        assertThat(DateUtil.rentDueDate(joined, YearMonth.of(2026, 1), 1)).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(DateUtil.rentDueDate(joined, YearMonth.of(2026, 3), 1)).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(DateUtil.rentDueDate(joined, YearMonth.of(2026, 3), 0)).isEqualTo(LocalDate.of(2026, 3, 15));
    }

    @Test
    void joiningDay31_clampedToShortMonths_withoutDriftingAfterFebruary() {
        assertThat(DateUtil.rentDueDate(JOINED_31_JAN, YearMonth.of(2026, 1), 0)).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(DateUtil.rentDueDate(JOINED_31_JAN, YearMonth.of(2026, 2), 0)).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(DateUtil.rentDueDate(JOINED_31_JAN, YearMonth.of(2026, 3), 0)).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(DateUtil.rentDueDate(JOINED_31_JAN, YearMonth.of(2026, 4), 0)).isEqualTo(LocalDate.of(2026, 4, 30));
        // Offset 1: February rent is due on 31 March
        assertThat(DateUtil.rentDueDate(JOINED_31_JAN, YearMonth.of(2026, 2), 1)).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void leapYearFebruary() {
        assertThat(DateUtil.rentDueDate(LocalDate.of(2027, 12, 30), YearMonth.of(2028, 2), 0))
                .isEqualTo(LocalDate.of(2028, 2, 29));
        assertThat(DateUtil.rentDueDate(LocalDate.of(2026, 1, 29), YearMonth.of(2026, 2), 0))
                .isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void noJoiningDate_dueOnFirstOfCollectionMonth() {
        assertThat(DateUtil.rentDueDate(null, YearMonth.of(2026, 9), 1)).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void mobileNumbers() {
        assertThat(MobileNumberUtil.normalizeIndianMobile("9876543210")).isEqualTo("9876543210");
        assertThat(MobileNumberUtil.normalizeIndianMobile("+91 98765 43210")).isEqualTo("9876543210");
        assertThat(MobileNumberUtil.normalizeIndianMobile("09876543210")).isEqualTo("9876543210");
        assertThat(MobileNumberUtil.normalizeIndianMobile("1234567890")).isNull();
        assertThat(MobileNumberUtil.normalizeIndianMobile("98765")).isNull();
        assertThat(MobileNumberUtil.normalizeIndianMobile(null)).isNull();
        assertThat(MobileNumberUtil.mask("9876543210")).isEqualTo("******3210");
    }
}
