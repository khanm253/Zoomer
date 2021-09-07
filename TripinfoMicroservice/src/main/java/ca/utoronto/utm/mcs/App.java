package ca.utoronto.utm.mcs;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class App {
   static int PORT = 8000;
   static String TAG = App.class.getSimpleName();

   public static void main(String[] args) throws IOException {

      // Connect to mongo database.
      MongoCredential credential = MongoCredential.createCredential("root", "admin", "123456".toCharArray());
      MongoClient mongoClient = new MongoClient(new ServerAddress("mongodb", 27017),
              Arrays.asList(credential));
      MongoDatabase tripDB = mongoClient.getDatabase("trip");

      // Create a trip collection if it does not exist
      if (!tripDB.listCollectionNames().iterator().hasNext()) {
         tripDB.createCollection("trips");
         System.out.println(TAG + ": created trip collection");
      }
      MongoCollection tripDBCollection = tripDB.getCollection("trips");

      HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
      server.createContext("/trip/request", new TripRequest(tripDBCollection));
      server.createContext("/trip/confirm", new TripConfirm(tripDBCollection));
      server.createContext("/trip", new TripUpdate(tripDBCollection));
      server.createContext("/trip/passenger",new TripsPassenger(tripDBCollection));
      server.createContext("/trip/driver",new TripsDriver(tripDBCollection));
      server.createContext("/trip/driverTime", new TripDriverTime(tripDBCollection));
      server.createContext("/trip/test/deleteTestTrips", new DeleteTestTrips(tripDBCollection));
      server.start();
      System.out.printf("Server started on port %d...\n", PORT);
   }
}
