package uk.org.metfriendly.service;

import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.org.metfriendly.model.UnauthorizedException;

@Component
public class IndividualIncomeService {

    private final ServiceConnector serviceConnector;

    @Value("${serverToken}")
    private String serverToken;

    @Autowired
    public IndividualIncomeService(ServiceConnector serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

    public String getAnnualIncomeSummary(String accessToken) throws UnauthorizedException {
        try {
        	String utr = "1111111111";
        	String taxYear="2014-15";
        	
            return serviceConnector.get(
                    "https://api.service.hmrc.gov.uk/individual-income/sa/"+utr+"/annual-summary/"+taxYear,
                    "application/vnd.hmrc.1.0+json",
                    Optional.of(accessToken));
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }
    }
}
