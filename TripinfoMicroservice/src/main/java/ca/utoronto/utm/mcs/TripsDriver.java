package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

public class TripsDriver implements HttpHandler {
    public static final String TAG = TripUpdate.class.getSimpleName();
    private MongoCollection mCollection;

    public TripsDriver(MongoCollection tripDBCollection) {
        mCollection = tripDBCollection;
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + exchange.getRequestURI().toString());
        try {
            if (exchange.getRequestMethod().equals("GET")) {
                getTripDriver(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTripDriver(HttpExchange r) throws IOException, JSONException {


        try {
            String[] requestPath = r.getRequestURI().getPath().split("/");
            if (requestPath.length != 4) {
                System.err.println(TAG + ": invalid path!");
                handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
                return;
            }

            String driverId = requestPath[3];

            // Check if driverId exists
            TripDAO tripDAO = new TripDAO(mCollection);
            if (!tripDAO.doesDriverExist(driverId)) {
                handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                return;
            }

            Duration dur = Duration.ofSeconds(-5000);
            System.out.println(String.format("%02d:%02d:%02d",
                    dur.toHours(), dur.toMinutesPart(), dur.toSecondsPart()));

            // generate the trips.
            JSONArray trips = new JSONArray();
            for (Trip trip : tripDAO.getTripsForDriver(driverId)) {
                JSONObject currTrip = new JSONObject();
                currTrip.put("distance", trip.getDistance());
                currTrip.put("driverPayout", trip.getDriverPayout());
                currTrip.put("endTime", trip.getEndTime());
                currTrip.put("startTime", trip.getStartTime());
                currTrip.put("timeElapsed", trip.getTimeElapsed());
                currTrip.put("passenger", trip.getPassenger());
                currTrip.put("_id", trip.getId());
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
