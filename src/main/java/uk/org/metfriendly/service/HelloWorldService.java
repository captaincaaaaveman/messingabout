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

    @Autowired
    public HelloWorldService(ServiceConnector serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

    public String helloWorld() {
        try {
            return serviceConnector.get(
                    "https://api.service.hmrc.gov.uk/hello/world",
                    "application/vnd.hmrc.1.0+json",
                    Optional.absent());
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }

    public String helloApplication() throws UnauthorizedException {
        return serviceConnector.get(
                "https://api.service.hmrc.gov.uk/hello/application",
                "application/vnd.hmrc.1.0+json",
                Optional.of(serverToken));
    }

    public String helloUser(String accessToken) throws UnauthorizedException {
        return serviceConnector.get(
                "https://api.service.hmrc.gov.uk/hello/user",
                "application/vnd.hmrc.1.0+json",
                Optional.of(accessToken));
    }

	public String discoverEndpoints(String accessToken) throws UnauthorizedException {
        return serviceConnector.get(
                "https://api.service.hmrc.gov.uk/lifetime-isa/manager/Z1126",
                "application/vnd.hmrc.1.0+json",
                Optional.of(accessToken));
	}
}
