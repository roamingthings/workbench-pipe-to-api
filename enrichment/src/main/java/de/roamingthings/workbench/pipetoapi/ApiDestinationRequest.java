package de.roamingthings.workbench.pipetoapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Serdeable
public record ApiDestinationRequest(@JsonProperty("Headers") Map<String, String> headers, @JsonProperty("RequestBody") String requestBody) {
    public ApiDestinationRequest(String body) {
        this(calcSignature(body), body);
    }

    private static Map<String, String> calcSignature(String body) {
        try {
            var md = MessageDigest.getInstance("SHA-1");
            var digest = md.digest(body.getBytes());
            var sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            var headers = new HashMap<String, String>();
            headers.put("signature", sb.toString());
            return headers;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }
}
