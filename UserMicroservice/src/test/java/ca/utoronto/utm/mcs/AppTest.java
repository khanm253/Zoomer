package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/*
Please Write Your Tests For CI/CD In This Class. 
You will see these tests pass/fail on github under github actions.
*/
public class AppTest {

   @BeforeAll
   public static void beforeAllTests() throws Exception {
      // Create a dummy object to test on.
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("name", "TESTING__testUser1")
              .put("email","TESTING__testUser1@test.com")
              .put("password", "TESTING__password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/register"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
   }

   @AfterAll
   public static void afterAllTests() throws Exception {
      HttpClient client = HttpClient.newHttpClient();
      // Delete test users
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .uri(URI.create("http://localhost:8004/user/test/deleteTestUsers"))
              .build();
      client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
   }

   /**
    * Endpoint: POST /user/register
    * Case: 200 (successful register)
    */
   @Test
   public void userRegister200Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("name", "TESTING__testUser2")
              .put("email","TESTING__testUser2@test.com")
              .put("password", "TESTING__password2")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/register"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
   }

   /**
    * Endpoint: POST /user/register
    * Case: 400 (user already exists)
    */
   @Test
   public void userRegister400_1_Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("name", "TESTING__testUser1")
              .put("email","TESTING__testUser1@test.com")
              .put("password", "TESTING__password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/register"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
   }

   /**
    * Endpoint: POST /user/register
    * Case: 400 (improper request)
    */
   @Test
   public void userRegister400_2_Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("name", "TESTING__testUser1")
              .put("email","TESTING__testUser1@test.com")
              .put("PASSWORD", "TESTING__password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/register"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
   }

   /**
    * Endpoint: POST /user/login
    * Case: 200 (correct username and password)
    */
   @Test
   public void userLogin200Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("email","TESTING__testUser1@test.com")
              .put("password", "TESTING__password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/login"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(200, responseCode);
      assertEquals("OK", responseStatus);
   }

   /**
    * Endpoint: POST /user/login
    * Case: 401 (correct username and incorrect password)
    */
   @Test
   public void userLogin401Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("email","TESTING__testUser1@test.com")
              .put("password", "TESTING__wrong_password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/login"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(401, responseCode);
      assertEquals("UNAUTHORIZED", responseStatus);
   }

   /**
    * Endpoint: POST /user/login
    * Case: 404 (username does not exist)
    */
   @Test
   public void userLogin404Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("email","TESTING__testUser3@test.com")
              .put("password", "TESTING__password3")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/login"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(404, responseCode);
      assertEquals("NOT FOUND", responseStatus);
   }

   /**
    * Endpoint: POST /user/login
    * Case: 400 (improper request)
    */
   @Test
   public void userLogin400Test() throws Exception{
      HttpClient client = HttpClient.newHttpClient();
      String requestBody = (new JSONObject())
              .put("EMAIL","TESTING__testUser1@test.com")
              .put("password", "TESTING__password1")
              .toString();
      HttpRequest httpRequest = HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(URI.create("http://localhost:8004/user/login"))
              .build();
      HttpResponse<String> apiResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JSONObject responseBody = new JSONObject(apiResponse.body());

      int responseCode = apiResponse.statusCode();
      String responseStatus = responseBody.getString("status");

      assertEquals(400, responseCode);
      assertEquals("BAD REQUEST", responseStatus);
   }



}
