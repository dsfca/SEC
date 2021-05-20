import org.junit.jupiter.api.Test;


import shared.TrackerLocationSystem;
import user.NormalUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;

import java.util.HashSet;
import java.util.Set;


public class UserTest {
	static int  num_users;
	static int G_width;
	static int G_height;
	static int f;
	static TrackerLocationSystem trl;
    
	
	//@BeforeAll
/*	static void setUpBeforeClass() throws Exception {
		  num_users = 10;
	        G_width = 3;
	        G_height = 3;
	        f = 0;
	        try {
	        	trl = new TrackerLocationSystem(num_users, G_width, G_height,f);
	        	trl.start();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}*/

    /***
    * This test simulate the report submitreport of user 2 in epoch 2 to server.
     * */
  /*  @Test
    void submitReportTest() {

       

        try {
       
            
            int userID = 2;
            int epoch = 2;
            NormalUser u = trl.getUsers().get(userID);
            Thread.sleep(1000);
            boolean submitStatus = u.proveLocation(epoch);
            assertEquals(submitStatus, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
    
   // @Test
 /*   void obtainLocationReportTest() {
    	try {

            int userID = 1;
            int epoch = 2;
            NormalUser u = trl.getUsers().get(userID);

            boolean serverReply = u.proveLocation(epoch);
            assertEquals(serverReply, true);
            Thread.sleep(2000);
            String reply =  u.obtainLocationReport(epoch);
            System.out.println("user position: " + reply);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}