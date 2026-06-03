package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        // default port is 8080, can be overridden by passing in a new port
        int port = 8080;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        // start the REPL loop
        new Repl(port).run();
    }
}
