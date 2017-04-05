	package uk.org.metfriendly.service;

import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.org.metfriendly.model.UnauthorizedException;

@Component
public class HelloWorldService {

    private final ServiceConnector serviceConnector;

    @Value("${serverToken}")
    private String serverToken;

    @Value("${apiRoot}")
    private String apiRoot;

    @Autowired
    public HelloWorldService(ServiceConnector serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

    public String helloWorld() {
        try {
            return serviceConnector.get(
            		apiRoot + "hello/world",
                    "application/vnd.hmrc.1.0+json",
                    Optional.absent());
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    public String helloApplication() throws UnauthorizedException {
        return serviceConnector.get(
        		apiRoot + "hello/application",
                "application/vnd.hmrc.1.0+json",
                Optional.of(serverToken));
    }

    public String helloUser(String accessToken) throws UnauthorizedException {
        return serviceConnector.get(
        		apiRoot + "hello/user",
                "application/vnd.hmrc.1.0+json",
                Optional.of(accessToken));
    }

}
