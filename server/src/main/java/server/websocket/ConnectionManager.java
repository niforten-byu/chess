package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * keeps track of games and who is connected to each game
 */
public class ConnectionManager {

    // map gameID to a map of authentication tokens -> session
    // data structure: gameID -> maps to -> (authenticationToken -> session)
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();

    // add a new user to active game ist
    public void add(int gameID, String authenticationToken, Session session) {
        // if the game doesn't have any active users when the user joined it, add it to active map
        var gameConnections = connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());

        // add user's authentication token and their session to game
        gameConnections.put(authenticationToken, session);
    }

    // remove user when they leave or disconnect form a game
    public void remove(int gameID, String authenticationToken) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            // remove authentication token for player from map
            gameConnections.remove(authenticationToken);
            if (gameConnections.isEmpty()) {
                // if no one is in the game, remove the game from the map
                connections.remove(gameID);
            }
        }
    }

    // send a message to everyone in a game except the person who sent the message
    public void broadcast(int gameID, String excludeAuthenticationToken, ServerMessage message) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            // convert java ServerMessage object to JSON string
            String jsonMessage = new Gson().toJson(message);

            // loop through each user in the game
            for (var entry : gameConnections.entrySet()) {
                String authToken = entry.getKey();
                Session session = entry.getValue();

                // send message to a user if the session is active and the user didn't trigger the message
                if (session.isOpen() && !authToken.equals(excludeAuthenticationToken)) {
                    session.getRemote().sendString(jsonMessage);
                }
            }
        }
    }
}