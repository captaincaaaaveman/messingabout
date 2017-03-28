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
import uk.org.metfriendly.service.LisaService;
import uk.org.metfriendly.service.OauthService;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Controller
public class LisaController {

    private final LisaService lisaService;
    private final OauthService oauthService;
    @Value("${clientId}")
    private String clientId;
    @Value("${callback.url}")
    private String callbackUrl;
    
    
    @Autowired
    public LisaController(LisaService lisaService, OauthService oauthService) {
        this.lisaService = lisaService;
        this.oauthService = oauthService;
    }

    @RequestMapping("/lifetime-isa/manager")
    @ResponseBody
    public String discoverEndpoints(HttpSession session, HttpServletResponse response) throws UnauthorizedException, IOException {
            if (session.getAttribute("userToken") == null || ! checkScope( "read:lisa", session )  ) {

                response.sendRedirect(getAuthorizationRequestUrl("read:lisa", "http://localhost:8080/oauth20/callback/lisa"));
                return "";

            } else {
                Token userToken = (Token) session.getAttribute("userToken");
                try {
                    return lisaService.discoverEndpoints(userToken.getAccessToken());
                } catch (UnauthorizedException ue) {
                    Token refreshedToken = oauthService.refreshToken(userToken.getRefreshToken());
                    session.setAttribute("userToken", refreshedToken);
                    return lisaService.discoverEndpoints(refreshedToken.getAccessToken());
                }
            }
        }
    
    
    private boolean checkScope(String scope, HttpSession session) {
		return scope.equals( session.getAttribute("scope") );
	}

	@RequestMapping("/oauth20/callback/lisa")
    public String callbackLisa(HttpSession session, @RequestParam("code") Optional<String> code, @RequestParam("error") Optional<String> error) {
        if (!code.isPresent()) {
            throw new RuntimeException("Couldn't get Authorization code: " + error.orElse("unknown_reason"));
        }
        try {
            Token token = oauthService.getToken(code.get(), callbackUrl + "/lisa");
            session.setAttribute("userToken", token);
        	session.setAttribute("scope", "read:lisa");
            return "redirect:/lifetime-isa/manager";
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
