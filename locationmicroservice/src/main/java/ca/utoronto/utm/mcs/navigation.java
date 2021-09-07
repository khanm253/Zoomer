package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import static org.neo4j.driver.Values.parameters;

public class navigation implements HttpHandler {

    public static final String TAG = nearbydriver.class.getSimpleName();

    @Override
    public void handle(HttpExchange r) {
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            if (r.getRequestMethod().equals("GET")) {
                getNavigationRoute(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO: MAKE SURE CHANGES IN STARTER CODE ARE INCORPORATED
    public void getNavigationRoute(HttpExchange r) throws IOException, JSONException {
        //Make sure path is of the correct length
        String[] requestPath = r.getRequestURI().getPath().split("/");
        if (requestPath.length != 4) {
            System.err.println(TAG + ": invalid path!");
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
            return;
        }

        System.out.println("THIS IS THE REQ LENGTH THIS: "+ requestPath.length);

        String duid = requestPath[3];;

        // Get the passenger uid
        String requestQuery = r.getRequestURI().getQuery();
        if ((requestQuery == null)) {
            System.err.println(TAG + ": invalid query provided!");
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
            return;
        }
        String puid = requestQuery.replaceFirst("passengerUid=","");

        System.out.println("THIS IS THE DUID: "+ duid);
        System.out.println("THIS IS THE PUID: "+ puid);

        if (!duid.isEmpty() && !puid.isEmpty()) {
            try (Session session = Utils.driver.session()) {

                String driverCheck = "MATCH (u :user) where u.uid=" + "'" + duid + "' RETURN u";
                String passengerCheck = "MATCH (u :user) where u.uid=" + "'" + puid + "' RETURN u";

                Result driverCheckResult = session.run(driverCheck);
                Result passengerCheckResult = session.run(passengerCheck);

                if(passengerCheckResult.hasNext() && driverCheckResult.hasNext()){

                    Record Drecord = driverCheckResult.next();
                    Record Precord = passengerCheckResult.next();

                    String roadStart = Drecord.get("u").get("street_at").asString();
                    String roadEnd = Precord.get("u").get("street_at").asString();

                    if(roadStart.isEmpty() || roadEnd.isEmpty()){
                        handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                        return;
                    }

                    //TODO: Have to fix query to make it work with the fix in starter code in route.java
                    Result minTimePathResult = session.run(
                            "MATCH sp=(a:road)-[ROUTE_TO*]->(b:road)\n" +
                            "WHERE a.name= $x AND b.name = $y \n" +
                            "WITH relationships(sp) AS roads, sp \n" +
                            "WITH reduce(d=0, r in roads | d + r.travel_time) as time, sp\n" +
                            "ORDER BY time\n" +
                            "RETURN min(time), sp",parameters("x",roadStart, "y", roadEnd));

                    if(minTimePathResult.hasNext()){
                        System.out.println("Min path exists"+ roadStart + roadEnd);
                        Record record = minTimePathResult.next();
                        JSONObject info = new JSONObject();

                        JSONArray routes = new JSONArray();
                        Iterator<Node> nodeIterator = record.get("sp").asPath().nodes().iterator();
                        Iterator<Relationship> rIterator = record.get("sp").asPath().relationships().iterator();

                        while(nodeIterator.hasNext() && rIterator.hasNext()){
                            Node node = nodeIterator.next();
                            JSONObject currRoad = new JSONObject();
                            if(node.get("name").asString().equals(roadStart)){
                                currRoad.put("street",node.get("name").asString());
                                currRoad.put("has_traffic",node.get("is_traffic").asBoolean());
                                currRoad.put("time", 0);
                                routes.put(currRoad);
                            }else{
                                currRoad.put("street",node.get("name").asString());
                                currRoad.put("has_traffic",node.get("is_traffic").asBoolean());
                                currRoad.put("time", rIterator.next().get("travel_time").asInt());
                                routes.put(currRoad);
                            }
                        }

                        JSONObject data = new JSONObject();
                        data.put("total_time",record.get("min(time)").asInt());
                        data.put("route", routes);
                        info.put("status","0K");
                        info.put("data",data);
                        handleResponse(r, 200, info);
                    }else{
                        handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                    }
                }else{
                    handleResponse(r, 404, new JSONObject().put("status", "NOT FOUND").put("data", new JSONObject()));
                }

            }catch(Exception e){
                handleResponse(r, 500, new JSONObject().put("status", "INTERNAL SERVER ERROR"));
            }
        }else{
            handleResponse(r, 400, new JSONObject().put("status", "BAD REQUEST").put("data", new JSONObject()));
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
