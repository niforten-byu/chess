package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        // start the server on random open port
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        // initialize the facade to point to the server's new port
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        // wipe database clean before every test
        facade.clear();
    }


    @Test
    public void registerSuccess() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Sung Jinwoo", "thePlayer", "weakestHunter@gmail.com"));
        Assertions.assertNotNull(authenticationData.authToken());
        Assertions.assertEquals("Sung Jinwoo", authenticationData.username());
    }

    @Test
    public void registerFailure() throws Exception {
        // register successfully
        facade.register(new UserData("Cha Hae-In", "theDancer", "everybodySmells@email.com"));

        // attempt to register the same user (this should throw an exception: 403 Already Taken)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(new UserData("Cha Hae-In", "theDancer", "everybodySmells@email.com"));
        });
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register(new UserData("Sung Il-Hwan", "padre", "badFather@email.com"));

        AuthData authenticationData = facade.login(new LoginRequest("Sung Il-Hwan", "padre"));
        Assertions.assertNotNull(authenticationData.authToken());
    }

    @Test
    public void loginFailure() throws Exception {
        facade.register(new UserData("Go Gunhee", "oldMan", "whatDoesThisGuyDO@email.com"));

        // attempt login with and password (throw an exception: 401 Unauthorized)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("Go Gunhee", "Incorrect_Password"));
        });
    }

    @Test
    public void logoutSuccess() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Goto Ryuji", "japanNumberOne", "iHopeIDontDie@email.com"));

        Assertions.assertDoesNotThrow(() -> facade.logout(authenticationData.authToken()));
    }

    @Test
    public void logoutFailure() throws Exception {
        // try to log out with a fake token (401 Unauthorized)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.logout("non-existent-token");
        });
    }

    @Test
    public void createGameSuccess() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Atsushi Kumamoto", "eyepatch", "beserker@email.com"));

        int gameId = facade.createGame(new CreateGameRequest("Practice Match"), authenticationData.authToken());
        Assertions.assertTrue(gameId > 0);
    }

    @Test
    public void createGameFailure() throws Exception {
        // try to make game with non-existent token
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame(new CreateGameRequest("Non-existent game"), "non-existent-token");
        });
    }

    @Test
    public void listGamesSuccess() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Kei", "iceman", "heDiedQuick@email.com"));
        facade.createGame(new CreateGameRequest("IceWall"), authenticationData.authToken());
        facade.createGame(new CreateGameRequest("HeadChoppedOff"), authenticationData.authToken());

        GameData[] games = facade.listGames(authenticationData.authToken());
        Assertions.assertEquals(2, games.length);
    }

    @Test
    public void listGamesFailure() throws Exception {
        // try to list games using a non-existent token
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames("non-existent-token");
        });
    }

    @Test
    public void joinGameSuccess() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Ma Dongwook", "bigMan", "lookAtMeImBig@email.com"));
        int gameId = facade.createGame(new CreateGameRequest("Big Man Fight"), authenticationData.authToken());

        Assertions.assertDoesNotThrow(() -> facade.joinGame(new JoinGameRequest("WHITE", gameId), authenticationData.authToken()));
    }

    @Test
    public void joinGameFailure() throws Exception {
        AuthData authenticationDataOne = facade.register(new UserData("Baek Yoonho", "whiteTiger", "theBeast@email.com"));
        AuthData authenticationDataTwo = facade.register(new UserData("Choi Jong-In", "fireBaby", "ultimateWeapon@email.com"));

        int gameId = facade.createGame(new CreateGameRequest("Warm Up Match"), authenticationDataOne.authToken());

        // player 1 joins as white
        facade.joinGame(new JoinGameRequest("WHITE", gameId), authenticationDataOne.authToken());

        // player two also tries to join as white (throw exception: 403 Already Taken)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.joinGame(new JoinGameRequest("WHITE", gameId), authenticationDataTwo.authToken());
        });
    }
}
