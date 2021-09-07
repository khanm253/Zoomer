package ca.utoronto.utm.mcs;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Please Write Your Tests For CI/CD In This Class. 
You will see these tests pass/fail on github under github actions.
*/
public class AppTest {

   @BeforeAll
   public static void beforeAllTests() throws Exception {
      afterAllTests();

      // Add dummy data to the database
      addTestRoad("TESTING__testRoad1", false);
      addTestRoad("TESTING__testRoad2", false);
      addTestRoad("TESTING__testRoad3", false);
      addTestRoad("TESTING__testRoad4", true);

      addTestRoutes("TESTING__testRoad1", "TESTING__testRoad2", true, 5);
      addTestRoutes("TESTING__testRoad1", "TESTING__testRoad3", true, 5);
      addTestRoutes("TESTING__testRoad3", "TESTING__testRoad2", true, 5);

      addTestUser("TESTING__testUser1",false,0.0,0.0,"TESTING__testRoad2");
      addTestUser("TESTING__testUser2",false,0.0,0.0,"TESTING__testRoad4");

      addTestUser("TESTING__testDriver1",true,5.0,5.0,"TESTING__testRoad1");
      addTestUser("TESTING__testDriver2",true,10.0,10.0,"TESTING__testRoad999");
      addTestUser("TESTING__testDriver3",true,3.0,2.0,"TESTING__testRoad4");

   }

   @AfterAll
   public static void afterAllTests() throws Exception {
      HttpClient client = HttpClient.newHttpClient();

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .uri(URI.create("http://localhost:8004/location/test/deleteTestObjects"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
   }

   /**
    * GET /location/navigation/test999?passenger=TESTING__testUser1
    * Case: driver not found
    */
   @Test
   public void navigationDriverNotFound() throws IOException, InterruptedException{

      int expectedStatusCode = 404;
      String expectedMessage = "{\"data\":{},\"status\":\"NOT FOUND\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "test999";
      String passengerUid = "TESTING__testUser1";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid + "?passengerUid=" + passengerUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedStatusCode,response.statusCode());
      assertEquals(expectedMessage,response.body());
   }

   /**
    * GET /location/navigation/TESTING__testDriver1?passenger=test999
    * Case: passenger does not exist in database.
    */
   @Test
   public void navigationPassengerNotFound() throws IOException, InterruptedException{

      int expectedStatusCode = 404;
      String expectedMessage = "{\"data\":{},\"status\":\"NOT FOUND\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "TESTING__testDriver1";
      String passengerUid = "test999";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid + "?passengerUid=" + passengerUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedStatusCode,response.statusCode());
      assertEquals(expectedMessage,response.body());
   }

   /**
    * GET /location/navigation/TESTING__testDriver2?passenger=test2
    * Case:road does not exist in database
    */
   @Test
   public void navigationRoadNotFound() throws IOException, InterruptedException{

      int expectedStatusCode = 404;
      String expectedMessage = "{\"data\":{},\"status\":\"NOT FOUND\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "TESTING__testDriver2";
      String passengerUid = "TESTING__testUser1";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid + "?passengerUid=" + passengerUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedStatusCode,response.statusCode());
      assertEquals(expectedMessage,response.body());
   }

   /**
    * GET /location/navigation/TESTING__testDriver1?passenger=TESTING__testUser2
    * Case:roads exist but route specified does not exist in database
    */
   @Test
   public void navigationRouteNotFound() throws IOException, InterruptedException{

      int expectedStatusCode = 404;
      String expectedMessage = "{\"data\":{},\"status\":\"NOT FOUND\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "TESTING__testDriver1";
      String passengerUid = "TESTING__testUser2";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid + "?passengerUid=" + passengerUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedStatusCode,response.statusCode());
      assertEquals(expectedMessage,response.body());
   }

   /**
    * GET /location/navigation/test1
    * Case: passengerUid not provided
    */
   @Test
   public void navigationParamNotPresent() throws IOException, InterruptedException{

      int expectedStatusCode = 400;
      String expectedMessage = "{\"data\":{},\"status\":\"BAD REQUEST\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "TESTING__testDriver1";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedStatusCode,response.statusCode());
      assertEquals(expectedMessage,response.body());
   }

   /**
    * GET /location/navigation/TESTING__testDriver1?passengerUid=TESTING__testUser1
    * Case: Response successfully provides, status, total time for
    * most efficient route of travel.
    */
   @Test
   public void navigationSuccessfulResponse() throws IOException, InterruptedException {

      int expectedStatusCode = 200;
      String expectedResponseBody = "{\"data\":{\"route\":" +
              "[{\"has_traffic\":false,\"street\":\"TESTING__testRoad1\",\"time\":0}," +
              "{\"has_traffic\":false,\"street\":\"TESTING__testRoad2\",\"time\":5}]," +
              "\"total_time\":5}," +
              "\"status\":\"0K\"}";
      HttpClient client = HttpClient.newHttpClient();

      String driverUid = "TESTING__testDriver1";
      String passengerUid = "TESTING__testUser1";

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/navigation/" + driverUid + "?passengerUid=" + passengerUid))
              .build();

      HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());

      //Assertions
      assertEquals(expectedResponseBody, response.body());
      assertEquals(expectedStatusCode,response.statusCode());
   }

   /**
    * Endpoint: GET /location/nearbyDriver/:uid?radius=
    * Case: 200
    */
   @Test
   public void tripRequest200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/nearbyDriver/TESTING__testUser1?radius=10"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      JSONObject responseData = responseBody.getJSONObject("data");

      boolean testDriver1Exists = false;
      boolean testDriver1Data = false;

      boolean testDriver3Exists = false;
      boolean testDriver3Data = false;

      Iterator<String> iterator = responseData.keys();
      while (iterator.hasNext()) {
         String key = iterator.next();
         if (key.equals("TESTING__testDriver1")) {
            testDriver1Exists = true;
         }
         else if (key.equals("TESTING__testDriver3")) {
            testDriver3Exists = true;
         }
      }


      if (testDriver1Exists) {
         JSONObject testDriver1= responseData.getJSONObject("TESTING__testDriver1");
         Double driver1Long = testDriver1.getDouble("longitude");
         Double driver1Lat = testDriver1.getDouble("latitude");
         String driver1Street = testDriver1.getString("street");

         if ((driver1Long == 5) && (driver1Lat == 5) && (driver1Street.equals("TESTING__testRoad1"))) {
            testDriver1Data = true;
         }
      }

      if (testDriver3Exists) {
         JSONObject testDriver3 = responseData.getJSONObject("TESTING__testDriver3");
         Double driver3Long = testDriver3.getDouble("longitude");
         Double driver3Lat = testDriver3.getDouble("latitude");
         String driver3Street = testDriver3.getString("street");

         if ((driver3Long == 2) && (driver3Lat == 3) && (driver3Street.equals("TESTING__testRoad4"))) {
            testDriver3Data = true;
         }
      }

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertTrue(testDriver1Data);
      assertTrue(testDriver1Exists);
      assertTrue(testDriver3Exists);
      assertTrue(testDriver3Data);
   }

   /**
    * Endpoint: GET /location/nearbyDriver/:uid?radius=
    * Case: 404 (The uid provided is not valid)
    */
   @Test
   public void tripRequest404_1_Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/nearbyDriver/TESTING__testUser99?radius=0.1"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      String responseData = responseBody.getJSONObject("data").toString();

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals(new JSONObject().toString(), responseData);
   }

   /**
    * Endpoint: GET /location/nearbyDriver/:uid?radius=
    * Case: 404 (No trips nearby)
    * NOTE: This method is hard to test if there is existing nodes in the db, and for that reason I will be commenting
    * it out. I have tested this method with is an empty database so I know our logic works.
    */
   @Test
   public void tripRequest404_2_Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/nearbyDriver/TESTING__testUser1?radius=0.1"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      String responseData = responseBody.getJSONObject("data").toString();

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals(new JSONObject().toString(), responseData);
   }

   /**
    * Endpoint: GET /location/nearbyDriver/:uid?radius=
    * Case: 400 (improper request)
    */
   @Test
   public void tripRequest400Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();

      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/location/nearbyDriver/TESTING__testUser1?radius=onekm"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      String responseData = responseBody.getJSONObject("data").toString();

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals(new JSONObject().toString(), responseData);
   }


   private static void addTestUser(String uid, boolean is_driver, double latitude, double longitude, String street) throws Exception {

      // Add test user to database
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("uid", uid)
              .put("is_driver",is_driver)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
                 .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                 .uri(URI.create("http://localhost:8004/location/user"))
                 .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      String requestBody2 = (new JSONObject())
              .put("longitude", longitude)
              .put("latitude",latitude)
              .put("street", street)
              .toString();
      HttpRequest httpRequest2 = HttpRequest.newBuilder()
              .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody2))
              .uri(URI.create("http://localhost:8004/location/" + uid))
              .build();
      client.send(httpRequest2, HttpResponse.BodyHandlers.ofString());
   }

   private static void addTestRoad(String roadName, boolean hasTraffic) throws Exception {

      // Add test road to database
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("roadName", roadName)
              .put("hasTraffic", hasTraffic)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/location/road"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
   }

   private static void addTestRoutes(String road1, String road2, boolean hasTraffic, int time) throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("roadName1", road1)
              .put("roadName2", road2)
              .put("hasTraffic",hasTraffic)
              .put("time", time)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/location/hasRoute"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
   }
}
