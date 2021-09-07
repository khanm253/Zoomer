package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Gateway implements HttpHandler {

    public static final String TAG = Gateway.class.getSimpleName();

    @Override
    public void handle(HttpExchange r)  {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-");
        System.out.println(TAG + ": Handling " + r.getRequestURI().toString());
        try {
            // Get the path
            String path = r.getRequestURI().getPath();
            // Get the query parameters (if it exists)
            String query = r.getRequestURI().getQuery();
            // Get the request body
            String requestBody = Utils.convert(r.getRequestBody());

            String url = r.getRequestURI().toString();
            String microservice;
            if (url.startsWith("/user/")) {
                microservice = "usermicroservice";
            }
            else if (url.startsWith("/location/")) {
                microservice = "locationmicroservice";
            }
            else if (url.startsWith("/trip/")) {
                microservice = "tripinfomicroservice";
            }
            else {
                handleResponse(r,404, new JSONObject().put("status", "INVALID ENDPOINT"));
                return;
            }
            // Make the new path
            URI uri = URI.create("http://" + microservice + ":8000" + path + ( query == null ? "" : "?" + query) );

            // forward request to apprioate microservice
            handleRequest(r, uri, requestBody);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(HttpExchange r, URI uri, String requestBody) throws JSONException, IOException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .method(r.getRequestMethod(), HttpRequest.BodyPublishers.ofString(requestBody))
                    .uri(uri)
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            handleResponse(r, response.statusCode(), new JSONObject(response.body()));
                        } catch (IOException|JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
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
