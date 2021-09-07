package ca.utoronto.utm.mcs;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/*
Please Write Your Tests For CI/CD In This Class. 
You will see these tests pass/fail on github under github actions.
*/
public class AppTest {

   private static String testTripId0 = "";
   private static String testTripId1 = "";

   @BeforeAll
   public static void beforeAllTests() throws Exception {
      afterAllTests();

      // Create dummy data for tests
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

      testTripId0 = addTestTrip(false, "TESTING__testDriver1", "TESTING__testUser1", 1000,
              -1, "", -1, -1, -1, -1);
      testTripId1 = addTestTrip(true, "TESTING__testDriver3", "TESTING__testUser2", 1000,
              12345, "00:15:00", 12, 0, 59.64, 21.56);
   }

   @AfterAll
   public static void afterAllTests() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      // Delete test users
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .uri(URI.create("http://localhost:8004/trip/test/deleteTestTrips"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      HttpRequest httpRequest2 = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .uri(URI.create("http://localhost:8004/location/test/deleteTestObjects"))
              .build();
      client.send(httpRequest2, HttpResponse.BodyHandlers.ofString());
   }


   /**
    * Endpoint: POST /trip/request
    * Case: 200
    */
   @Test
   public void tripRequest200_1_Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("uid", "TESTING__testUser1")
              .put("radius",10.0)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/request"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONArray responseData = responseBody.getJSONArray("data");

      boolean driver1Exists = false;
      boolean driver3Exists = false;

      for (int i=0; i < responseData.length() ; i++) {
         String driver = responseData.getString(i);

         if (driver.equals("TESTING__testDriver1")) {
            driver1Exists = true;
         }
         else if (driver.equals("TESTING__testDriver3")) {
            driver3Exists = true;
         }
      }

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertTrue(driver1Exists);
      assertTrue(driver3Exists);

   }

   /**
    * Endpoint: POST /trip/request
    * Case: 404 (No Drivers in radius)
    */
   @Test
   public void tripRequest404_1_Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("uid", "TESTING__testUser1")
              .put("radius",0.1)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/request"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONArray responseData = responseBody.getJSONArray("data");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals("[]", responseData.toString());

   }

   /**
    * Endpoint: POST /trip/request
    * Case: 404 (invalid passenger uid)
    */
   @Test
   public void tripRequest404_2_Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("uid", "TESTING__testDriver9999")
              .put("radius",50.14)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/request"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONArray responseData = responseBody.getJSONArray("data");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals("[]", responseData.toString());
   }

   /**
    * Endpoint: POST /trip/request
    * Case: 400 (Bad Request)
    */
   @Test
   public void tripRequest400Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("uid", "TESTING__testDriver9999")
              .put("radius","fifity")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/request"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONArray responseData = responseBody.getJSONArray("data");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals("[]", responseData.toString());
   }

   /**
    * Endpoint: POST /trip/confirm
    * Case: 200
    */
   @Test
   public void tripConfirm200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("driver", "TESTING__testDriver2")
              .put("passenger","TESTING__testUser2")
              .put("startTime", 1000)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/confirm"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      String responseId = responseBody.getJSONObject("data").getString("_id");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertNotEquals("", responseId);
   }

   /**
    * Endpoint: POST /trip/confirm
    * Case: 400 (improper request)
    */
   @Test
   public void tripConfirm400_1_Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("DRIVER", "TESTING__testDriver2")
              .put("passenger","TESTING__testUser2")
              .put("startTime", 1000)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/confirm"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals("{}", responseData.toString());
   }

   /**
    * Endpoint: POST /trip/confirm
    * Case: 400 (duplicate trip)
    */
   @Test
   public void tripConfirm400_2_Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("driver", "TESTING__testDriver1")
              .put("passenger","TESTING__testUser1")
              .put("startTime", 1000)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/confirm"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals("{}", responseData.toString());
   }

   /**
    * Endpoint: PATCH /trip/:_id
    * Case: 200 (successful update)
    */
   @Test
   public void tripUid200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("distance", 12)
              .put("endTime",12345)
              .put("timeElapsed", "00:15:00")
              .put("discount", 0)
              .put("totalCost", 59.64)
              .put("driverPayout", 21.56)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/" + testTripId0))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
   }

   /**
    * Endpoint: PATCH /trip/:_id
    * Case: 400 (improper request)
    */
   @Test
   public void tripUid400Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("distance", 12)
              .put("endTime",12345)
              .put("timeElapsed", "00:15:00")
              .put("discount", 0)
              .put("totalCost", "fifty dollars")
              .put("driverPayout", 21.56)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/" + testTripId0))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
   }

   /**
    * Endpoint: PATCH /trip/:_id
    * Case: 404 (trip does not exist)
    */
   @Test
   public void tripUid404Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("distance", 12)
              .put("endTime",12345)
              .put("timeElapsed", "00:15:00")
              .put("discount", 0)
              .put("totalCost", 64.11)
              .put("driverPayout", 21.56)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/" + testTripId0 + "randomId"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
   }

   /**
    * Endpoint: GET /trip/passenger/:uid
    * Case: 200 (successful update)
    */
   @Test
   public void tripPassenger200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/passenger/TESTING__testUser2"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      JSONArray responseTrips = responseBody.getJSONObject("data").getJSONArray("trips");
      JSONObject testTrip1 = responseTrips.getJSONObject(0);

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      String responseId = testTrip1.getString("_id");
      double responseDistance = testTrip1.getDouble("distance");
      double responseCost = testTrip1.getDouble("totalCost");
      int responseDiscount = testTrip1.getInt("discount");
      long responseStartTime = testTrip1.getLong("startTime");
      long responseEndTime = testTrip1.getLong("endTime");
      String responseTimeElapsed = testTrip1.getString("timeElapsed");
      String responseDriver = testTrip1.getString("driver");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertEquals(testTripId1, responseId);
      assertEquals(12,responseDistance);
      assertEquals( 59.64,responseCost);
      assertEquals( 0,responseDiscount);
      assertEquals( 1000, responseStartTime);
      assertEquals( 12345, responseEndTime);
      assertEquals( "00:15:00", responseTimeElapsed);
      assertEquals("TESTING__testDriver3", responseDriver);
   }

   /**
    * Endpoint: GET /trip/passenger/:uid
    * Case: 400 (improper request)
    */
   @Test
   public void tripPassenger400Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/passenger/TESTING__test/User1"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());


      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals("{}",responseData.toString());
   }

   /**
    * Endpoint: GET /trip/passenger/:uid
    * Case: 404 (uid not found)
    */
   @Test
   public void tripPassenger404Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/passenger/TESTING__testUser332"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());


      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals("{}",responseData.toString());
   }

   /**
    * Endpoint: GET /trip/driver/:uid
    * Case: 200 (successful update)
    */
   @Test
   public void tripDriver200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/driver/TESTING__testDriver3"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      JSONArray responseTrips = responseBody.getJSONObject("data").getJSONArray("trips");
      JSONObject testTrip1 = responseTrips.getJSONObject(0);

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      String responseId = testTrip1.getString("_id");
      double responseDistance = testTrip1.getDouble("distance");
      double responseDriverPayout = testTrip1.getDouble("driverPayout");
      long responseStartTime = testTrip1.getLong("startTime");
      long responseEndTime = testTrip1.getLong("endTime");
      String responseTimeElapsed = testTrip1.getString("timeElapsed");
      String responsePassenger = testTrip1.getString("passenger");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertEquals(testTripId1, responseId);
      assertEquals( 12, responseDistance);
      assertEquals( 21.56, responseDriverPayout);
      assertEquals( 1000, responseStartTime);
      assertEquals( 12345, responseEndTime);
      assertEquals( "00:15:00", responseTimeElapsed);
      assertEquals("TESTING__testUser2", responsePassenger);
   }

   /**
    * Endpoint: GET /trip/driver/:uid
    * Case: 400 (improper request)
    */
   @Test
   public void tripDriver400Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/driver/TESTING__test/Driver1"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());


      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
      assertEquals("{}",responseData.toString());
   }

   /**
    * Endpoint: GET /trip/driver/:uid
    * Case: 404 (uid not found)
    */
   @Test
   public void tripDriver404Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/driver/TESTING__testDriver332"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      JSONObject responseData = responseBody.getJSONObject("data");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals("{}",responseData.toString());
   }

   /**
    * Endpoint: GET /trip/driver/:uid
    * Case: 200 (successful)
    */
   @Test
   public void tripDriverTime200Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/driverTime/" + testTripId0))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      JSONObject responseData = responseBody.getJSONObject("data");

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");
      int responseArrivalTime = responseData.getInt("arrival_time");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
      assertEquals(5, responseArrivalTime);
   }

   /**
    * Endpoint: GET /trip/driver/:uid
    * Case: 404 (trip id is not valid)
    */
   @Test
   public void tripDriverTime404Test() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create("http://localhost:8004/trip/driverTime/" + testTripId0 + "invalid"))
              .build();

      HttpResponse<String> apiResponse = client.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());
      JSONObject responseData = responseBody.getJSONObject("data");

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
      assertEquals("{}", responseData.toString());
   }

   private static String addTestTrip(boolean patch, String driver, String passenger, long startTime, long endTime, String timeElapsed,
                                   double distance, int discount, double totalCost, double driverPayout) throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("driver", driver)
              .put("passenger",passenger)
              .put("startTime", startTime)
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/trip/confirm"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      String _id = (new JSONObject(apiResponse.body())).getJSONObject("data").getString("_id");

      if (patch) {
         String requestBody2 = (new JSONObject())
                 .put("distance", distance)
                 .put("endTime", endTime)
                 .put("timeElapsed", timeElapsed)
                 .put("discount", discount)
                 .put("totalCost", totalCost)
                 .put("driverPayout", driverPayout)
                 .toString();
         HttpRequest httpRequest2 = HttpRequest.newBuilder()
                 .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody2))
                 .uri(URI.create("http://localhost:8004/trip/" + _id))
                 .build();
         client.send(httpRequest2, HttpResponse.BodyHandlers.ofString());
      }
      return _id;
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
