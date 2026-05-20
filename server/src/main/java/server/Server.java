package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;

public class Server {

    // instantiate the DAOs and Services for the whole server
    private final MemoryUserDAO userDAO = new MemoryUserDAO();
    private final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    private final MemoryGameDAO gameDAO = new MemoryGameDAO();
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

    private Javalin javalin;

    public Server() {
        // need to implement
    }

    public int run(int desiredPort) {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // register your endpoints
        javalin.delete("/db", this::clearHandler);

        javalin.start(desiredPort);
        return javalin.port();
    }

    // handler method
    private void clearHandler(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result("{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }
    }

    public void stop() {
        javalin.stop();
    }
}