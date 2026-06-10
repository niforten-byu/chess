package websocket;

import websocket.messages.ServerMessage;

/**
 * interface that allows background websocket to notify the repl
 * when a message arrives from the server
 */
public interface ServerMessageObserver {
    void notify(ServerMessage message);
}