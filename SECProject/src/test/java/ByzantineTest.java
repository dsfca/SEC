import org.junit.jupiter.api.Test;
import server.Server;
import shared.Point2D;
import shared.TrackerLocationSystem;
import user.ByzantineUser;

import java.util.Scanner;

public class ByzantineTest {

    /***
     * This test creates one byzantine user, who eiter acts on behalf of user1 (broadcasts and submits report)
     * or wants to get a proof of fake position.
     * */
    @Test
    void differentPlace() throws Exception {

        int num_users = 10;
        int G_width = 5;
        int G_height = 5;
        int serverPort = 9100;

/*        try {
            TrackerLocationSystem.setServerPort(serverPort);
            TrackerLocationSystem.ini_pos_file(num_users, 5, G_width, G_height);
            TrackerLocationSystem.start_users(num_users-1, G_width, G_height);

            Server server = new Server(serverPort);

            ByzantineUser byzantineUser = new ByzantineUser(num_users-1);

            new Scanner(System.in).nextLine();

        } catch (Exception e) {
            System.out.println("Location system went wrong");
            e.printStackTrace();
        }*/



    }

}