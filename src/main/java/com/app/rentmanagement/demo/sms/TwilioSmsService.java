package com.app.rentmanagement.demo.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * Twilio Programmable Messaging (sms.provider=twilio). A trial account sends free messages,
 * but only to numbers verified in the Twilio console.
 *
 * sms.account-sid is the Account SID and sms.api-key the Auth Token; both come from
 * environment variables and are never logged.
 */
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsService implements SmsService {

    private static final String DEFAULT_API_URL = "https://api.twilio.com";

    private final RestClient restClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String countryCode;

    public TwilioSmsService(@Value("${sms.api-url:}") String apiUrl,
                            @Value("${sms.account-sid:}") String accountSid,
                            @Value("${sms.api-key:}") String authToken,
                            @Value("${sms.sender-id:}") String fromNumber,
                            @Value("${sms.country-code:91}") String countryCode,
                            @Value("${sms.timeout-seconds:10}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl == null || apiUrl.isBlank() ? DEFAULT_API_URL : apiUrl.trim())
                .requestFactory(requestFactory)
                .build();
        this.accountSid = trim(accountSid);
        this.authToken = trim(authToken);
        this.fromNumber = trim(fromNumber);
        this.countryCode = trim(countryCode);
    }

    @Override
    public SmsResponse sendSms(String mobileNumber, String message) {
        if (accountSid.isEmpty() || authToken.isEmpty() || fromNumber.isEmpty()) {
            return SmsResponse.failed("Twilio is not configured (SMS_ACCOUNT_SID, SMS_API_KEY, SMS_SENDER_ID)");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", "+" + countryCode + mobileNumber);
        form.add("From", fromNumber);
        form.add("Body", message);

        try {
            Map<?, ?> body = restClient.post()
                    .uri("/2010-04-01/Accounts/{sid}/Messages.json", accountSid)
                    .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            Object sid = body != null ? body.get("sid") : null;
            return SmsResponse.sent(sid != null ? sid.toString() : null);
        } catch (RestClientResponseException e) {
            // Twilio's error body (code + message) contains no credentials
            return SmsResponse.failed("Twilio API error " + e.getStatusCode().value() + ": "
                    + abbreviate(e.getResponseBodyAsString()));
        } catch (ResourceAccessException e) {
            return SmsResponse.failed("Twilio network error or timeout: " + e.getMessage());
        } catch (RestClientException e) {
            return SmsResponse.failed("Twilio request failed: " + e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return "twilio";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
