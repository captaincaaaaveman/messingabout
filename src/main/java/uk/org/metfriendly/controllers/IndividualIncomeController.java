package uk.org.metfriendly.controllers;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.org.metfriendly.model.Token;
import uk.org.metfriendly.model.UnauthorizedException;
import uk.org.metfriendly.service.HelloWorldService;
import uk.org.metfriendly.service.IndividualIncomeService;
import uk.org.metfriendly.service.OauthService;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@Controller
public class IndividualIncomeController {

    private final IndividualIncomeService individualIncomeService;
    private final OauthService oauthService;
    @Value("${clientId}")
    private String clientId;
    @Value("${callback.url}")
    private String callbackUrl;

    @Autowired
    public IndividualIncomeController(HelloWorldService helloWorldService, IndividualIncomeService individualIncomeService, OauthService oauthService) {
        this.individualIncomeService = individualIncomeService;
        this.oauthService = oauthService;
    }

    @RequestMapping("/annual-income")
    @ResponseBody
    public String getAnnualIncomeSummary(HttpSession session, HttpServletResponse response) throws UnauthorizedException, IOException {
        if (session.getAttribute("userToken") == null) {

            response.sendRedirect(getAuthorizationRequestUrl("read:individual-income", "http://localhost:8080/oauth20/callback/annual-income"));
            return "";

        } else {
            Token userToken = (Token) session.getAttribute("userToken");
            try {
                return individualIncomeService.getAnnualIncomeSummary(userToken.getAccessToken());
            } catch (UnauthorizedException ue) {
                Token refreshedToken = oauthService.refreshToken(userToken.getRefreshToken());
                session.setAttribute("userToken", refreshedToken);
                return individualIncomeService.getAnnualIncomeSummary(refreshedToken.getAccessToken());
            }
        }
    }
    
    @RequestMapping("/oauth20/callback/annual-income")
    public String callbackAnnualIncome(HttpSession session, @RequestParam("code") Optional<String> code, @RequestParam("error") Optional<String> error) {
        if (!code.isPresent()) {
            throw new RuntimeException("Couldn't get Authorization code: " + error.orElse("unknown_reason"));
        }
        try {
            Token token = oauthService.getToken(code.get(), callbackUrl + "/annual-income");
            session.setAttribute("userToken", token);
            return "redirect:/annual-income";
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Token", e);
        }
    }

    private String getAuthorizationRequestUrl( String scope, String specialCallback ) {
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(oauthService.authorizeUrl)
                    .setResponseType("code")
                    .setClientId(clientId)
                    .setScope(scope)
                    .setRedirectURI(specialCallback)
                    .buildQueryMessage();
            return request.getLocationUri();

        } catch (OAuthSystemException e) {
            throw new RuntimeException(e);
        }
    }
        
}
