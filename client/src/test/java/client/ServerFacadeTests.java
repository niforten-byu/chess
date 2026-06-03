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
    public void registerPositive() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Sung Jinwoo", "thePlayer", "weakestHunter@gmail.com"));
        Assertions.assertNotNull(authenticationData.authToken());
        Assertions.assertEquals("Sung Jinwoo", authenticationData.username());
    }

    @Test
    public void registerNegative() throws Exception {
        // register successfully
        facade.register(new UserData("Cha Hae-In", "theDancer", "everybodySmells@email.com"));

        // attempt to register the same user (this should throw an exception: 403 Already Taken)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(new UserData("Cha Hae-In", "theDancer", "everybodySmells@email.com"));
        });
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register(new UserData("Sung Il-Hwan", "padre", "badFather@email.com"));

        AuthData authenticationData = facade.login(new LoginRequest("Sung Il-Hwan", "padre"));
        Assertions.assertNotNull(authenticationData.authToken());
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register(new UserData("Go Gunhee", "oldMan", "whatDoesThisGuyDO@email.com"));

        // attempt login with and password (throw an exception: 401 Unauthorized)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("Go Gunhee", "Incorrect_Password"));
        });
    }

    @Test
    public void logoutPositive() throws Exception {
        AuthData authenticationData = facade.register(new UserData("Goto Ryuji", "japanNumberOne", "iHopeIDontDie@email.com"));

        Assertions.assertDoesNotThrow(() -> facade.logout(authenticationData.authToken()));
    }

    @Test
    public void logoutNegative() throws Exception {
        // try to log out with a fake token (401 Unauthorized)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.logout("non-existent-token");
        });
    }

}
