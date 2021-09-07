package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class Login implements HttpHandler {

    public static final String TAG = Login.class.getSimpleName();
    public Connection connection;

    public Login(Connection connection)  {
        this.connection = connection;
    }


    @Override
    public void handle(HttpExchange r) {
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                loginUser(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loginUser(HttpExchange r) throws NoSuchAlgorithmException, InvalidKeySpecException, JSONException, IOException {
        try {
            // Get the body parameters - email, password
            String requestBody = Utils.convert(r.getRequestBody());
            JSONObject jsonRequestBody = new JSONObject(requestBody);

            String email = jsonRequestBody.getString("email");
            String password = jsonRequestBody.getString("password");
            String hashedPassword = Utils.convertStringToHash(password);

            try {

                if (doesUserExist(email)) {
                    // Check if the passwords match.
                    PreparedStatement ps = connection.prepareStatement(
                            "SELECT password FROM users WHERE email=?");
                    ps.setString(1,email);
                    ResultSet resultSet = ps.executeQuery();
                    resultSet.next();
                    String databasePassword = resultSet.getString("password");
                    ps.close();
                    ps = null;

                    if (hashedPassword.equals(databasePassword)) {
                        handleResponse(r, 200, new JSONObject().put("status", "OK"));
                    }
                    else {
                        handleResponse(r, 401, new JSONObject().put("status", "UNAUTHORIZED"));
                    }

                }
                else {
                    handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
            }

        } catch (IOException| JSONException e) {
            e.printStackTrace();
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
        }

    }

    private boolean doesUserExist(String email) throws SQLException {
        PreparedStatement psCheck = connection.prepareStatement("SELECT email FROM users WHERE email=?");
        psCheck.setString(1, email);
        ResultSet rsCheck = psCheck.executeQuery();
        boolean doesExist = rsCheck.next();
        psCheck.close();
        rsCheck = null;
        psCheck = null;
        return doesExist;
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
