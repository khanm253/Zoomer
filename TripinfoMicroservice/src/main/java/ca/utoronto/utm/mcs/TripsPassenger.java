package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class TripsPassenger implements HttpHandler {

    public static final String TAG = TripUpdate.class.getSimpleName();
    private MongoCollection mCollection;

    public TripsPassenger(MongoCollection tripDBCollection) {
        mCollection = tripDBCollection;
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + exchange.getRequestURI().toString());
        try {
            if (exchange.getRequestMethod().equals("GET")) {
                getTripPassenger(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTripPassenger(HttpExchange r) throws JSONException, IOException {

        try {
            String[] requestPath = r.getRequestURI().getPath().split("/");
            if (requestPath.length != 4) {
                System.err.println(TAG + ": invalid path!");
                handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
                return;
            }

            String passengerId = requestPath[3];

            // Check if passengerId exists
            TripDAO tripDAO = new TripDAO(mCollection);
            if (!tripDAO.doesPassengerExist(passengerId)) {
                handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                return;
            }

            // generate the trips.
            JSONArray trips = new JSONArray();
            for (Trip trip : tripDAO.getTripsForPassenger(passengerId)) {
                JSONObject currTrip = new JSONObject();
                currTrip.put("distance", trip.getDistance());
                currTrip.put("totalCost", trip.getTotalCost());
                currTrip.put("endTime", trip.getEndTime());
                currTrip.put("startTime", trip.getStartTime());
                currTrip.put("timeElapsed", trip.getTimeElapsed());
                currTrip.put("driver", trip.getDriver());
                currTrip.put("_id", trip.getId());
                currTrip.put("discount", trip.getDiscount());
                trips.put(currTrip);
            }

            JSONObject data = new JSONObject();
            data.put("trips", trips);

            // Process response from query
            JSONObject response = new JSONObject();
            response.put("status", "OK");
            response.put("data", data);

            handleResponse(r, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
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
