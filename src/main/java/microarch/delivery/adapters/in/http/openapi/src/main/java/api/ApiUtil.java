package api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.context.request.NativeWebRequest;

public final class ApiUtil {

    private ApiUtil() {
    }

    public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
        HttpServletResponse response = req.getNativeResponse(HttpServletResponse.class);
        if (response == null) {
            return;
        }

        try {
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Type", contentType);
            response.getWriter().print(example);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write example response", e);
        }
    }
}
