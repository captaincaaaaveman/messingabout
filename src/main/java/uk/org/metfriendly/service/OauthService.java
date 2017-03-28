package uk.org.metfriendly.service;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.org.metfriendly.model.Token;

@Component
public class OauthService {

    private final OAuthClient oAuthClient;

    public final String authorizeUrl;
    public final String tokenUrl;

    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;

    public OauthService() {
        this.oAuthClient = new OAuthClient(new URLConnectionClient());
        authorizeUrl = "https://api.service.hmrc.gov.uk/oauth/authorize";
        tokenUrl = "https://api.service.hmrc.gov.uk/oauth/token";
    }

    public Token getToken(String code, String callbackUrl) {
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(tokenUrl)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectURI(callbackUrl)
                    .setCode(code)
                    .buildBodyMessage();

            return fetchToken(request);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Token refreshToken(String refreshToken) {
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(tokenUrl)
                    .setGrantType(GrantType.REFRESH_TOKEN)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .buildBodyMessage();

            return fetchToken(request);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Token fetchToken(OAuthClientRequest tokenRequest) throws OAuthProblemException, OAuthSystemException {
        OAuthJSONAccessTokenResponse tokenResponse = oAuthClient.accessToken(tokenRequest);
        return new Token(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }
}
