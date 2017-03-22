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
import uk.org.metfriendly.service.OauthService;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@Controller
public class HelloWorldController {

    private final HelloWorldService helloWorldService;
    private final OauthService oauthService;
    @Value("${clientId}")
    private String clientId;
    @Value("${callback.url}")
    private String callbackUrl;

    @Autowired
    public HelloWorldController(HelloWorldService helloWorldService, OauthService oauthService) {
        this.helloWorldService = helloWorldService;
        this.oauthService = oauthService;
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/hello-world")
    @ResponseBody
    public String helloWorld() {
        return helloWorldService.helloWorld();
    }

    @RequestMapping("/hello-application")
    @ResponseBody
    public String helloApplication() {
        try {
            return helloWorldService.helloApplication();
        } catch (UnauthorizedException e) {
            throw new RuntimeException("Unauthorized request. Check that server_token is set.");
        }
    }

    @RequestMapping("/oauth20/callback")
    public String callback(HttpSession session, @RequestParam("code") Optional<String> code, @RequestParam("error") Optional<String> error) {
        if (!code.isPresent()) {
            throw new RuntimeException("Couldn't get Authorization code: " + error.orElse("unknown_reason"));
        }
        try {
            Token token = oauthService.getToken(code.get());
            session.setAttribute("userToken", token);
            return "redirect:/hello-user";
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Token", e);
        }
    }

    @RequestMapping("/hello-user")
    @ResponseBody
    public String helloUser(HttpSession session, HttpServletResponse response) throws UnauthorizedException, IOException {
        if (session.getAttribute("userToken") == null) {

            response.sendRedirect(getAuthorizationRequestUrl());
            return "";

        } else {
            Token userToken = (Token) session.getAttribute("userToken");
            try {
                return doCallHelloUser(userToken.getAccessToken());
            } catch (UnauthorizedException ue) {
                Token refreshedToken = oauthService.refreshToken(userToken.getRefreshToken());
                session.setAttribute("userToken", refreshedToken);
                return doCallHelloUser(refreshedToken.getAccessToken());
            }
        }
    }

    private String getAuthorizationRequestUrl() {
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(oauthService.authorizeUrl)
                    .setResponseType("code")
                    .setClientId(clientId)
                    .setScope("hello")
                    .setRedirectURI(callbackUrl)
                    .buildQueryMessage();
            return request.getLocationUri();

        } catch (OAuthSystemException e) {
            throw new RuntimeException(e);
        }
    }

    private String doCallHelloUser(String token) throws UnauthorizedException {
        return helloWorldService.helloUser(token);
    }
}
