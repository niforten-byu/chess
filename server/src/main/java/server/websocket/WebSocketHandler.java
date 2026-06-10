package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;


/**
 * Network traffic controller for websocket messages. When client sends a message
 * over a websocket connection Javalin routes it to onMessage method
 */
public class WebSocketHandler {
    // keep track of every user's open network session
    public final ConnectionManager connections = new ConnectionManager();

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
        // take game id, authentication token and session and store it (so we can access it to send messages to them later)
        connections.add(command.getGameID(), command.getAuthToken(), context.session);
        System.out.println("User connected to WebSocket for game: " + command.getGameID());
    }

    private void makeMove(WsMessageContext context, UserGameCommand command) {}
    private void leave(WsMessageContext context, UserGameCommand command) {}
    private void resign(WsMessageContext context, UserGameCommand command) {}
}
