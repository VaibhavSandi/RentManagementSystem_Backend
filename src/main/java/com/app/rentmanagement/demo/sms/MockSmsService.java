package com.app.rentmanagement.demo.sms;

import com.app.rentmanagement.demo.util.MobileNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Development provider (sms.provider=mock, the default): logs the SMS instead of sending it. */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsService implements SmsService {

    @Override
    public SmsResponse sendSms(String mobileNumber, String message) {
        log.info("SMS MOCK\nTo: {}\nMessage: {}", MobileNumberUtil.mask(mobileNumber), message);
        return SmsResponse.sent("mock-" + UUID.randomUUID());
    }

    @Override
    public String providerName() {
        return "mock";
    }
}
