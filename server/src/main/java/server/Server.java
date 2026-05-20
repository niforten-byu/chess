package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import service.UserService;
import java.util.Map;

public class Server {

    // instantiate the DAOs and Services for the whole server
    private final MemoryUserDAO userDAO = new MemoryUserDAO();
    private final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    private final MemoryGameDAO gameDAO = new MemoryGameDAO();
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    private final UserService userService = new UserService(userDAO, authDAO);

    private Javalin javalin;

    public Server() {
        // need to implement
    }

    public int run(int desiredPort) {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // register your endpoints
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);

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

    private void registerHandler(Context ctx) {
        try {
            // translate incoming JSON into a UserData record
            UserData user = new Gson().fromJson(ctx.body(), UserData.class);

            AuthData auth = userService.register(user);

            // translate new AuthData record into JSON
            ctx.status(200);
            ctx.result(new Gson().toJson(auth));

        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            } else if (e.getMessage().equals("Error: already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }

            // return error in JSON
            ctx.result(new Gson().toJson(Map.of("message", e.getMessage())));
        }
    }

    public void stop() {
        javalin.stop();
    }
}