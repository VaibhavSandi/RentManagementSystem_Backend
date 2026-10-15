package com.app.rentmanagement.demo.sms;

/**
 * Sends a text message through the configured provider (sms.provider).
 * Implementations never throw: provider, network and timeout errors are returned
 * as a failed SmsResponse so one bad send can't stop a batch.
 */
public interface SmsService {

    /** @param mobileNumber 10-digit Indian mobile number, already validated */
    SmsResponse sendSms(String mobileNumber, String message);

    /** Provider name for logs and API results, e.g. "mock" or "twilio". */
    String providerName();
}
