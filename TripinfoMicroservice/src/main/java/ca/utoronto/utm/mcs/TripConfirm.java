package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;


public class TripConfirm implements HttpHandler {

    public static final String TAG = TripConfirm.class.getSimpleName();
    private MongoCollection mCollection;

    public TripConfirm(MongoCollection collection) {
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
            // Get the body parameters from the request (driver, passenger, startTime)
            String requestBody = Utils.convert(r.getRequestBody());
            JSONObject jsonRequestBody = new JSONObject(requestBody);
            String driver = jsonRequestBody.getString("driver");
            String passenger = jsonRequestBody.getString("passenger");
            long startTime = jsonRequestBody.getLong("startTime");

            try {
                // Invalid start time
                if (startTime < 0) {
                    handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
                    return;
                }

                // Check if trip is duplicated
                TripDAO tripDAO = new TripDAO(mCollection);
                if (tripDAO.isTripDuplicated(driver, passenger, startTime)) {
                    handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
                    return;
                }

                // Create the Trip Object
                Trip newTrip = new Trip();
                newTrip.setDriver(driver);
                newTrip.setPassenger(passenger);
                newTrip.setStartTime(startTime);

                // Add it to the mongo db
                Trip trip = tripDAO.createTrip(newTrip);

                // Send back response
                JSONObject response = new JSONObject();
                response.put("status", "OK");
                response.put("data", new JSONObject().put("_id", trip.getId()));
                handleResponse(r, 200, response);

            } catch (Exception e) {
                handleResponse(r, 500, new JSONObject().put("status", "SERVER ERROR"));
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
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
