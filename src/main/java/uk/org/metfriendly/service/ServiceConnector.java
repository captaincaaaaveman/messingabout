package uk.org.metfriendly.service;

import com.google.common.base.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import uk.org.metfriendly.model.UnauthorizedException;

import java.io.IOException;

@Component
public class ServiceConnector {

    private final HttpClient client;

    public ServiceConnector() {
        client = HttpClientBuilder.create().build();;
    }

    public String get(String url, String acceptHeader, Optional<String> bearerToken) throws UnauthorizedException {
        HttpGet request = new HttpGet(url);
        request.addHeader("Accept", acceptHeader);
        if (bearerToken.isPresent()) {
            request.addHeader("Authorization", "Bearer " + bearerToken.get());
        }

        try {
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == 401) {
                throw new UnauthorizedException();
            }
            return EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
