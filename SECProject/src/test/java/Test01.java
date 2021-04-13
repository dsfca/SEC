import org.junit.jupiter.api.Test;
import server.Server;
import shared.TrackerLocationSystem;

import java.util.Scanner;

public class Test01 {

    @Test
    void simpleTest() {

        int num_users = 10;
        int G_width = 5;
        int G_height = 5;
        int serverPort = 9100;

        try {
            TrackerLocationSystem.setServerPort(serverPort);
            TrackerLocationSystem.ini_pos_file(num_users, 5, G_width, G_height);
            TrackerLocationSystem.start_users(num_users, G_width, G_height);

            Server server = new Server(serverPort);

            new Scanner(System.in).nextLine();


        } catch (Exception e) {
            System.out.println("Location system went wrong");
            e.printStackTrace();
        }


    }

}
