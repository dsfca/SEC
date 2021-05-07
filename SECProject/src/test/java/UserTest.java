import org.junit.jupiter.api.Test;


import shared.TrackerLocationSystem;
import user.NormalUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;


public class UserTest {
	static int  num_users;
	static int G_width;
	static int G_height;
	static int f;
	static TrackerLocationSystem trl;
    
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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
	}

    /***
    * This test simulate the report submitreport of user 2 in epoch 2 to server.
     * */
    @Test
    void submitReportTest() {

       

        try {
       
            
            int userID = 2;
            int epoch = 2;
            NormalUser u = trl.getUsers().get(userID);
            Thread.sleep(1000);
            String serverReply = u.proveLocation(epoch); u.proveLocation(epoch);
            assertEquals(serverReply, "Your report was submitted successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    @Test
    void obtainLocationReportTest() {
    	try {
    		
            int userID = 1;
            int epoch = 2;
            NormalUser u = trl.getUsers().get(userID);
            String serverReply = u.proveLocation(epoch); u.proveLocation(epoch);    
            assertEquals(serverReply, "Your report was submitted successfully");
            Thread.sleep(2000);
            String reply =  u.obtainLocationReport(epoch);
            System.out.println("user position: " + reply);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}