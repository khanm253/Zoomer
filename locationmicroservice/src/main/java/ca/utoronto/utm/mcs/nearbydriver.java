package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.OutputStream;

import static org.neo4j.driver.Values.parameters;


public class nearbydriver implements HttpHandler {

    public static final String TAG = nearbydriver.class.getSimpleName();


    @Override
    public void handle(HttpExchange r) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("GET")) {
                getNearbyDriver(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getNearbyDriver(HttpExchange r) throws IOException, JSONException {
        // Get the uid
        String[] requestPath = r.getRequestURI().getPath().split("/");
        if (requestPath.length != 4) {
            System.err.println(TAG + ": invalid path!");
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
            return;
        }
        String uid = requestPath[3];

        // Get the radius
        String requestQuery = r.getRequestURI().getQuery();
        if ((requestQuery == null) || (!requestQuery.matches("^radius=[0-9]*\\.?[0-9]+$"))) {
            System.err.println(TAG + ": invalid query provided!");
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
            return;
        }
        Double radius = Double.parseDouble(requestQuery.replaceFirst("radius=",""));

        // Get the nearby drivers from neo4j
        try (Session session = Utils.driver.session()) {

            Result result = session.run(
                    "MATCH (u1:user), (u2:user) " +
                    "WHERE u1.uid=$x AND u1.is_driver=FALSE AND u2.is_driver=TRUE AND " +
                    "EXISTS { " +
                    "    MATCH (u1),(u2) " +
                    "    WHERE sqrt( (u2.longitude-u1.longitude)^2 + (u2.latitude-u1.latitude)^2 )  <= $y " +
                    "} " +
                    "RETURN u2", parameters("x", uid, "y", radius));

            // In the case the uid does not exist or there are no trips in the radius
            if (!result.hasNext()) {
                handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                return;
            }

            JSONObject data = new JSONObject();

            boolean flag = false;
            while (result.hasNext()) {
                flag = true;
                Record record = result.next();
                JSONObject driverInfo = new JSONObject();
                driverInfo.put("longitude", record.get("u2").get("longitude").asDouble());
                driverInfo.put("latitude", record.get("u2").get("latitude").asDouble());
                driverInfo.put("street", record.get("u2").get("street_at").asString());
                data.put(record.get("u2").get("uid").asString(),driverInfo);
            }

            if (flag) {
                handleResponse(r, 200, new JSONObject().put("data", data).put("status", "OK"));
            }
            else {
                handleResponse(r, 404, new JSONObject().put("data", data).put("status", "NOT FOUND"));
            }

        } catch (Exception e) {
            System.err.println(TAG + ": server error!");
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
