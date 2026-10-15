package com.app.rentmanagement.demo.sms;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsResponse {

    private final boolean success;

    // Provider's message id, when it returns one
    private final String providerMessageId;

    private final String errorMessage;

    public static SmsResponse sent(String providerMessageId) {
        return new SmsResponse(true, providerMessageId, null);
    }

    public static SmsResponse failed(String errorMessage) {
        return new SmsResponse(false, null, errorMessage);
    }
}
