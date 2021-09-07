package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class DeleteTestTrips implements HttpHandler {

    public static final String TAG = DeleteTestTrips.class.getSimpleName();
    private MongoCollection mCollection;

    public DeleteTestTrips(MongoCollection collection) {
        mCollection = collection;
    }

    @Override
    public void handle(HttpExchange r) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                postTripConfirm(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postTripConfirm(HttpExchange r) throws IOException, JSONException {

        try {
            // Delete trips
            TripDAO tripDAO = new TripDAO(mCollection);
            tripDAO._deleteTestTrips();

            // Send back response
            JSONObject response = new JSONObject();
            response.put("status", "OK");
            handleResponse(r, 200, response);

        } catch (Exception e) {
            handleResponse(r, 500, new JSONObject().put("status", "SERVER ERROR"));
        }

    }

    private void handleResponse(HttpExchange request, int statusCode, JSONObject jsonObject) throws IOException {
        String response = jsonObject.toString();
        System.out.println(TAG +  ": Returning " + response);
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        request.sendResponseHeaders(statusCode, response.length());
        OutputStream os = request.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
