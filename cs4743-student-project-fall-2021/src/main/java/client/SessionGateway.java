package client;

import exceptions.Alerts;
import exceptions.UnauthorizedException;
import exceptions.UnknownException;
import mvc.model.Session;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SessionGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String token;
    private static final String WS_URL = "http://localhost:8080";

    public static Session authenticate(String userName, String hashedPassword) throws UnauthorizedException {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;

        try {
            httpclient = HttpClients.createDefault();

            JSONObject credentials = new JSONObject();
            credentials.put("password", hashedPassword);
            credentials.put("username", userName);

            HttpPost loginRequest = new HttpPost(WS_URL + "/login");
            String credentialsString = credentials.toString();
            LOGGER.info("Credentials: " + credentialsString + " has been sent.");
            StringEntity reqEntity = new StringEntity(credentialsString);

            loginRequest.setEntity(reqEntity);
            loginRequest.setHeader("Accept", "application/json");
            loginRequest.setHeader("Content-type", "application/json");
            response = httpclient.execute(loginRequest);
            switch (response.getStatusLine().getStatusCode()) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    //EntityUtils.consume(entity);
                    String sessionId = "";
                    int userId = 0;

                    JSONObject json = new JSONObject(strResponse);
                    sessionId = json.getString("sessionId");
                    userId = json.getInt("userId");

                    Session session = new Session(sessionId,userId);
                    session.setUrl(WS_URL);
                    LOGGER.info("Session Token: " +session.getSessionId()+".");
                    return session;
                case 401:
                    LOGGER.error("Invalid credentials.");
                    Alerts.infoAlert("Login Error","Authentication failed.","");
                    throw new UnauthorizedException("Invalid Credentials.");
                default:
                    LOGGER.error("Unknown error has occurred.");
                    throw new UnknownException(response.getStatusLine().getReasonPhrase());
            }

        } catch (IOException | UnknownException e) {
            throw new UnauthorizedException(e.getMessage());

        }
    }
}
