package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;
import service.GameService;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.CreateGameRequest;
import model.LoginRequest;
import service.UserService;
import java.util.Map;

public class Server {

    // instantiate the DAOs and Services for the whole server
    private final MemoryUserDAO userDAO = new MemoryUserDAO();
    private final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    private final MemoryGameDAO gameDAO = new MemoryGameDAO();
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);

    private Javalin javalin;

    public Server() {
        // need to implement
    }

    public int run(int desiredPort) {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // register your endpoints
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.post("/game", this::createGameHandler);

        javalin.start(desiredPort);
        return javalin.port();
    }

    // handler method
    private void clearHandler(Context contxt) {
        try {
            clearService.clear();
            contxt.status(200);
            contxt.result("{}");
        } catch (DataAccessException e) {
            contxt.status(500);
            contxt.result("{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }
    }

    private void registerHandler(Context context) {
        try {
            // translate incoming JSON into a UserData record
            UserData user = new Gson().fromJson(context.body(), UserData.class);

            AuthData auth = userService.register(user);

            // translate new AuthData record into JSON
            context.status(200);
            context.result(new Gson().toJson(auth));

        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                context.status(400);
            } else if (e.getMessage().equals("Error: already taken")) {
                context.status(403);
            } else {
                context.status(500);
            }

            // return error in JSON
            context.result(new Gson().toJson(Map.of("message", e.getMessage())));
        }
    }

    private void loginHandler(Context context) {
        try {
            // turn JSON into LoginRequest
            LoginRequest request = new Gson().fromJson(context.body(), LoginRequest.class);

            AuthData authentication = userService.login(request);

            context.status(200);
            context.result(new Gson().toJson(authentication));
        }
        catch (DataAccessException e) {
            if (e.getMessage().equals("Error: unauthorized")) {
                context.status(401);
            } else if (e.getMessage().equals("Error: bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            context.result(new Gson().toJson(Map.of("message", e.getMessage())));
        }
    }

    private void logoutHandler(Context context) {
        try {// get token from HTTP header
            String authenticationToken = context.header("authorization");
            userService.logout(authenticationToken);

            // return empty JSON
            context.status(200);
            context.result("{}");
        }
        catch (DataAccessException e) {
            if (e.getMessage().equals("Error: unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }
            context.result(new Gson().toJson(Map.of("message", e.getMessage())));
        }
    }

    private void createGameHandler (Context context) {
        try {
            // get token from header
            String authenticationToken = context.header("authorization");

            // convert JSON in CreateGameRecord
            CreateGameRequest request = new Gson().fromJson(context.body(), CreateGameRequest.class);

            // get new game id from service
            int gameID = gameService.createGame(authenticationToken, request.gameName());

            // return success
            context.status(200);
            context.result(new Gson().toJson(Map.of("gameID", gameID)));
        }
        catch (DataAccessException e) {
            if (e.getMessage().equals("Error: unauthorized")) {
                context.status(401);
            } else if (e.getMessage().equals("Error: bad request")) {
                context.status(400);
            } else {
                context.status(500);
            }
            context.result(new Gson().toJson(Map.of("message", e.getMessage())));
        }
    }

    public void stop() {
        javalin.stop();
    }
}