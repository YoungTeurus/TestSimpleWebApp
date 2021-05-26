package servlets;

import com.google.gson.Gson;
import models.error.ErrorMessage;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public interface Servlet {

    default void sendError(ErrorMessage error, HttpServletResponse response) throws IOException {
        response.setStatus(error.getError());
        sendObject(error, response);
    }

    default void sendObject(Object object, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String JSON = new Gson().toJson(object);
        PrintWriter out = response.getWriter();
        out.print(JSON);
        out.flush();
    }
}
