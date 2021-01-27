package ums_springboot_api.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ums_springboot_api.api.Pojos.ResponsePayload;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerletResponseUtil {

    public static void setServeletResponse(HttpServletResponse response, int statusCode, ResponsePayload payload) throws IOException {
        response.setStatus(statusCode);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(payload));
    }
}
