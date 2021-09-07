package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteTestUsers implements HttpHandler {

    public static final String TAG = DeleteTestUsers.class.getSimpleName();
    public Connection connection;

    public DeleteTestUsers(Connection connection) {
        this.connection = connection;
    }


    @Override
    public void handle(HttpExchange r) {
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                deleteTestUsers(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTestUsers(HttpExchange r) throws JSONException, IOException {
        try {

            // Only add new users to database
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM users WHERE email LIKE 'TESTING%'");
            ps.executeUpdate();
            ps.close();
            ps = null;
            handleResponse(r, 200, new JSONObject().put("status", "OK"));

        } catch (IOException| JSONException e) {
            e.printStackTrace();
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
        } catch (SQLException e) {
            e.printStackTrace();
            handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
        }

    }

    private void handleResponse(HttpExchange request, int statusCode, JSONObject jsonObject) throws IOException {
        String response = jsonObject.toString();
        System.out.println(TAG +  ": Returning " + response);
        request.sendResponseHeaders(statusCode, response.length());
        OutputStream os = request.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
