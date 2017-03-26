package uk.org.metfriendly.service;

import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.org.metfriendly.model.UnauthorizedException;

@Component
public class LisaService {

    private final ServiceConnector serviceConnector;

    @Value("${serverToken}")
    private String serverToken;
    @Value("${lisaManagerReferenceNumber}")
    private String lisaManagerReferenceNumber;

    @Autowired
    public LisaService(ServiceConnector serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

	public String discoverEndpoints(String accessToken) throws UnauthorizedException {
        return serviceConnector.get(
                "https://api.service.hmrc.gov.uk/lifetime-isa/manager/"+lisaManagerReferenceNumber,
                "application/vnd.hmrc.1.0+json",
                Optional.of(accessToken));
	}
}
