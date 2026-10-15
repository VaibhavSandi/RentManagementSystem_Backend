package com.app.rentmanagement.demo.util;

import java.util.regex.Pattern;

public class MobileNumberUtil {

    private static final Pattern INDIAN_MOBILE = Pattern.compile("[6-9]\\d{9}");

    private MobileNumberUtil() {}

    /**
     * Returns the 10-digit Indian mobile number, or null when it isn't a valid one.
     * Accepts spaces, dashes and a +91 / 91 / 0 prefix, e.g. "+91 98765-43210".
     */
    public static String normalizeIndianMobile(String mobileNumber) {
        if (mobileNumber == null) {
            return null;
        }
        String digits = mobileNumber.replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return INDIAN_MOBILE.matcher(digits).matches() ? digits : null;
    }

    /** For logs: "******3210". */
    public static String mask(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(mobileNumber.length() - 4) + mobileNumber.substring(mobileNumber.length() - 4);
    }
}
