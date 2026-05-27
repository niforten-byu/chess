package server;

import dataaccess.DatabaseManager;

public class ServerMain {
    public static void main(String[] args) {

        // sql connection test
        try {
            DatabaseManager.createDatabase();
            System.out.println("successful database connection ");
        } catch (Exception e) {
            System.err.println("database connection failure: " + e.getMessage());
        }

        Server server = new Server();
        server.run(8080);

        System.out.println("♕ 240 Chess Server");
    }
}