package ums_springboot_api.api.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ums_springboot_api.api.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UMS_SERVICE_CENTER {

    // make http request
    public static boolean sendMail(String url, String payload) {
        Boolean result = false;
        JSONObject parsedResponse = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parse = new JSONParser();
            parsedResponse = (JSONObject) parse.parse(response.body());

            System.out.println("mail sent");
            result = true;

        } catch (Exception e) {
            System.out.println("error ==> " + e.getMessage());
        }

        return result;
    }

//    SET EMAIL PARAMETERS
    public static JSONObject setEmailParamters(String mailTo, String subject, String content){
        JSONObject payload = new JSONObject();

        payload.put("mailTo", mailTo);
        payload.put("mailSubject", subject);
        payload.put("mailContent", content);
        payload.put("personnel", AppConfig.NO_REPLY_PERSONNEL);
        payload.put("mailFrom", AppConfig.MAIL_FROM);

        return payload;
    }
}
