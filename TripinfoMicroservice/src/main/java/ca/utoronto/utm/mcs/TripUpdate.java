package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;

public class TripUpdate implements HttpHandler {
    public static final String TAG = TripUpdate.class.getSimpleName();
    private MongoCollection mCollection;

    public TripUpdate(MongoCollection tripDBCollection) {
        mCollection = tripDBCollection;
    }

    @Override
    public void handle(HttpExchange r) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("PATCH")) {
                patchTripUpdate(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void patchTripUpdate(HttpExchange r) throws JSONException, IOException {

        try {
            String[] requestPath = r.getRequestURI().getPath().split("/");
            if (requestPath.length != 3) {
                System.err.println(TAG + ": invalid path!");
                handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
                return;
            }

            // Get the tripId and find the details of trip using it
            String tripId = requestPath[2];
            TripDAO tripDAO = new TripDAO(mCollection);
            Trip trip = tripDAO.getTrip(new ObjectId(tripId));
            if (trip == null) {
                handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND"));
                return;
            }

            //Process body of request
            String requestBody = Utils.convert(r.getRequestBody());
            JSONObject jsonRequestBody = new JSONObject(requestBody);

            // TODO check if timeElapsed format is corrrect (maybe)

            double distance = jsonRequestBody.getDouble("distance");
            double totalCost = jsonRequestBody.getDouble("totalCost");
            long endTime = jsonRequestBody.getLong("endTime");
            String timeElapsed = jsonRequestBody.getString("timeElapsed");
            double driverPayout = jsonRequestBody.getDouble("driverPayout");
            int discount = jsonRequestBody.getInt("discount");

            if ((discount < 0) || (discount > 100) || (distance < 0) || (totalCost < 0) || (endTime < 0) || (driverPayout < 0)) {
                handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
                return;
            }


            //Create Trip object
            Trip newTrip = new Trip();
            newTrip.setDiscount(discount);
            newTrip.setEndTime(endTime);
            newTrip.setDriverPayout(driverPayout);
            newTrip.setTimeElapsed(timeElapsed);
            newTrip.setTotalCost(totalCost);
            newTrip.setDistance(distance);
            newTrip.setDriver(trip.getDriver());
            newTrip.setPassenger(trip.getPassenger());
            newTrip.setStartTime(trip.getStartTime());

            //Perform the update
            UpdateResult result = tripDAO.updateTrip(TripConverter.toDocument(trip), TripConverter.toDocument(newTrip));

            if (result.getModifiedCount() == 1) {
                JSONObject response = new JSONObject();
                response.put("status", "OK");
                handleResponse(r, 200, response);
                return;
            }

            handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));

        } catch (JSONException e) {
            e.printStackTrace();
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND"));
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
