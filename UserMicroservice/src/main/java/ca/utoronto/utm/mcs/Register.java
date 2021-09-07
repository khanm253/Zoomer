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

public class Register implements HttpHandler {

    public static final String TAG = Register.class.getSimpleName();
    public Connection connection;

    public Register(Connection connection) {
        this.connection = connection;
    }


    @Override
    public void handle(HttpExchange r) {
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                registerUser(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerUser(HttpExchange r) throws NoSuchAlgorithmException, InvalidKeySpecException, JSONException, IOException {
        try {
            // Get the body parameters - name, email, password
            String requestBody = Utils.convert(r.getRequestBody());
            JSONObject jsonRequestBody = new JSONObject(requestBody);

            String name = jsonRequestBody.getString("name");
            String email = jsonRequestBody.getString("email");
            String password = jsonRequestBody.getString("password");
            String hashedPassword = Utils.convertStringToHash(password);

            try {

                // Only add new users to database
                if (!doesUserExist(email)) {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO users(email,prefer_name,password,rides,isdriver,availablecoupons, redeemedcoupons) VALUES (?, ?, ?, 0, False, '{}', '{}')");
                    ps.setString(1,email);
                    ps.setString(2,name);
                    ps.setString(3,hashedPassword);
                    ps.executeUpdate();
                    ps.close();
                    ps = null;
                    handleResponse(r, 200, new JSONObject().put("status", "OK"));
                }
                else {
                    handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
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
