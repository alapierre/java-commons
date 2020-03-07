package io.alapierre.freshmail;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 12.06.18
 */
@Slf4j
public class AuthInterceptor implements RequestInterceptor {

    private MessageDigest md;
    private String apiKey;
    private String apiSecret;

    public AuthInterceptor(String apiKey, String apiSecret) {

        this.apiKey = apiKey;
        this.apiSecret = apiSecret;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        requestTemplate.header("X-Rest-ApiKey", apiKey);
        requestTemplate.header("X-Rest-ApiSign", makeSign(requestTemplate));

        System.out.println(requestTemplate.request().method());
        System.out.println(requestTemplate.request().url());

        if(requestTemplate.request().body() != null) {
            System.out.println(new String(requestTemplate.request().body()));
        }
    }

    private String makeSign(RequestTemplate requestTemplate) {
        StringBuilder sb = new StringBuilder();
        sb.append(apiKey);
        sb.append(requestTemplate.request().url());
        if(requestTemplate.request().body() != null) {
            sb.append(new String(requestTemplate.request().body()));
        }
        sb.append(apiSecret);
        return sha1(sb.toString());
    }

    protected String sha1(String message) {

        byte[] result = md.digest(message.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
