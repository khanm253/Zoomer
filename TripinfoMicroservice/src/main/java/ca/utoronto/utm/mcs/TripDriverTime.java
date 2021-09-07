package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TripDriverTime implements HttpHandler {

    public static final String TAG = TripDriverTime.class.getSimpleName();
    private MongoCollection mCollection;

    public TripDriverTime(MongoCollection tripDBCollection) {
        mCollection = tripDBCollection;
    }

    @Override
    public void handle(HttpExchange exchange)  {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + exchange.getRequestURI().toString());
        try {
            if (exchange.getRequestMethod().equals("GET")) {
                getDriverTime(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDriverTime(HttpExchange r) throws JSONException, IOException {

        // Get the driver uid
        String[] requestPath = r.getRequestURI().getPath().split("/");
        if (requestPath.length != 4) {
            System.err.println(TAG + ": invalid path!");
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
            return;
        }
        try {
            ObjectId _id = new ObjectId(requestPath[3]);

            // From the Trip object get the driver uid and passenger id
            TripDAO tripDAO = new TripDAO(mCollection);
            Trip trip = tripDAO.getTrip(_id);

            if (trip == null) {
                handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                return;
            }

            String driver = trip.getDriver();
            String passenger = trip.getPassenger();

            try {
                // Make a request to the location microservice to get total time information
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://apigateway:8000/location/navigation/" + driver + "?passengerUid=" + passenger))
                        .build();
                HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // In the case the route does not exist or the driver or passenger uid do not exist.
                if (apiResponse.statusCode() != 200) {
                    handleResponse(r, apiResponse.statusCode(), new JSONObject(apiResponse.body()));
                    return;
                }

                // Get the total time from the return body
                int totalTime = (new JSONObject(apiResponse.body())).getJSONObject("data").getInt("total_time");

                // Handle Response
                JSONObject data = new JSONObject();
                data.put("arrival_time", totalTime);
                handleResponse(r, 200, new JSONObject().put("status", "OK").put("data", data));

            } catch (Exception e) {
                e.printStackTrace();
                handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
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
