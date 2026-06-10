package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;


/**
 * Network traffic controller for websocket messages. When client sends a message
 * over a websocket connection Javalin routes it to onMessage method
 */
public class WebSocketHandler {
    // keep track of every user's open network session
    public final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onMessage(WsMessageContext context) {
        try {
            // deserialize incoming JSON message into a UserGameCommand from client
            UserGameCommand command = new Gson().fromJson(context.message(), UserGameCommand.class);

            // route  command to correct method
            switch (command.getCommandType()) {
                case CONNECT -> connect(context, command);
                case MAKE_MOVE -> makeMove(context, command);
                case LEAVE -> leave(context, command);
                case RESIGN -> resign(context, command);
            }
        } catch (Exception e) {
            // catch broken JSON messages
            e.printStackTrace();
        }
    }


    private void connect(WsMessageContext context, UserGameCommand command) {
        try {
            // verify the authentication token
            AuthData authentication = authDAO.getAuthentication(command.getAuthToken());
            if (authentication == null) {
                // if token is bad, send error message back directly to the user
                context.send(new Gson().toJson(new ErrorMessage("Error: unauthorized")));
                return;
            }

            // verify game exists
            GameData game = gameDAO.getGame(command.getGameID());
            if (game == null) {
                context.send(new Gson().toJson(new ErrorMessage("Error: game not found")));
                return;
            }

            // add user to ConnectionManager
            connections.add(command.getGameID(), command.getAuthToken(), context.session);

            // send LOAD_GAME back to user who just connected
            LoadGameMessage loadMessage = new LoadGameMessage(game);
            context.send(new Gson().toJson(loadMessage));

            // broadcast a notification to everyone else in the game
            String username = authentication.username();
            String role = "an observer"; // default to observer

            // check if they are actually a player by looking at the database record
            if (username.equals(game.whiteUsername())) {
                role = "White";
            } else if (username.equals(game.blackUsername())) {
                role = "Black";
            }

            String message = String.format("%s joined the game as %s.", username, role);
            NotificationMessage notification = new NotificationMessage(message);

            // send to everyone except user who joined
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeMove(WsMessageContext context, UserGameCommand command) {}
    private void leave(WsMessageContext context, UserGameCommand command) {}
    private void resign(WsMessageContext context, UserGameCommand command) {}
}
