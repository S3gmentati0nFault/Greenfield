import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import extra.Logger.Logger;

import java.io.IOException;

import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException {
        adminServerJoinTest();

    }

    private static void adminServerJoinTest() throws IOException {
        Logger.test("adminServerJoinTest");
        final String HOST = "localhost";
        final int PORT = 8000;
        HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: http://"+HOST+":"+PORT);
    }
}
