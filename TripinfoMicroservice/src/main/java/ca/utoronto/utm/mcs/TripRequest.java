package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TripRequest implements HttpHandler {
    public static final String TAG = TripRequest.class.getSimpleName();
    private MongoCollection mCollection;


    public TripRequest(MongoCollection collection) {
        mCollection = collection;
    }

    @Override
    public void handle(HttpExchange r) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("POST")) {
                postTripRequest(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postTripRequest(HttpExchange r) throws IOException, JSONException {
        try {
            // Get the uid, and radius from the request body
            String body = Utils.convert(r.getRequestBody());
            JSONObject jsonRequestBody = new JSONObject(body);

            String uid = jsonRequestBody.getString("uid");
            Double radius = jsonRequestBody.getDouble("radius");

            // Send a request to apigateway microservice
            try {

                TripDAO tripDAO = new TripDAO(mCollection);

                if (!tripDAO.doesPassengerExist(uid)) {
                    handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONArray()));
                    return;
                }

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://apigateway:8000/location/nearbyDriver/" + uid + "?radius=" + radius))
                        .build();
                HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // Create the response json object
                JSONObject jsonApiResponse = new JSONObject(apiResponse.body());

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("status", jsonApiResponse.get("status"));

                JSONArray driverUids = new JSONArray();
                if (jsonApiResponse.has("data")) {
                    Iterator<String> apiData = jsonApiResponse.getJSONObject("data").keys();
                    while (apiData.hasNext()) {
                        driverUids.put(apiData.next());
                    }
                }
                jsonResponse.put("data", driverUids);
                handleResponse(r, apiResponse.statusCode(), jsonResponse);
                
            } catch (Exception e) {
                e.printStackTrace();
                handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONArray()));
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
