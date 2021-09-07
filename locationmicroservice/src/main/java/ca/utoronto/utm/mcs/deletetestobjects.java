package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.OutputStream;

public class deletetestobjects implements HttpHandler {


    public static final String TAG = deletetestobjects.class.getSimpleName();

    @Override
    public void handle(HttpExchange r) {
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                postDeleteTestObjects(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postDeleteTestObjects(HttpExchange r) throws JSONException, IOException {

        try (Session session = Utils.driver.session()) {
            session.run("MATCH (u:user) where u.uid STARTS WITH \"TESTING__\" DETACH DELETE u");
            session.run("MATCH (r:road) where r.name STARTS WITH \"TESTING__\" DETACH DELETE r");
            handleResponse(r, 200, new JSONObject().put("status", "OK"));
        } catch (Exception e) {
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
