package com.app.rentmanagement.demo.util;



import java.math.BigDecimal;

public class CommonUtil {

    private CommonUtil() {}

    public static BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}