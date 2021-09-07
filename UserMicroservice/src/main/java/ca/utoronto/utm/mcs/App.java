package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App {
   static int PORT = 8000;
   static Connection connection;

   public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

      String url = "jdbc:postgresql://postgres:5432/root";
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection(url, "root", "123456");

      HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
      server.createContext("/user/register", new Register(connection));
      server.createContext("/user/login", new Login(connection));
      server.createContext("/user", new User(connection));
      server.createContext("/user/test/deleteTestUsers", new DeleteTestUsers(connection));
      server.start();
      System.out.printf("Server started on port %d...\n", PORT);
   }

}
