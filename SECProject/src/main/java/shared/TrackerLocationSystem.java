package shared;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import server.Server;
import user.User;
import user.UserLocation;

public class TrackerLocationSystem {
	
	private static List<User> users = new ArrayList<User>();
	private static final String pos_file_path = "resources/Grid.txt";
	private static int currentEpoch = 0;
	private static int serverPort;

	public TrackerLocationSystem(int num_users, int G_width, int G_height, int server_port) throws Exception {
		TrackerLocationSystem.serverPort = server_port;

		start_server(server_port);
		start_users(num_users, G_width, G_height);

		//update user's epoch
		while(true) {
			Thread.sleep(15000);
			currentEpoch++;
			updateUsersEpoch(currentEpoch);
		}
	}
	
	public static void start_users(int num_users, int g_width, int g_height) throws Exception {
		for(int i = 0; i < num_users; i++) {
			User user = new User(i, g_width, g_height);
			users.add(user);
		}
	}

	public static void start_server(int port) {
		Server server = new Server(port);
	}
	
	/**************************************************************************************
	 * 										-update_user_pos()
	 * - this procedure writes the position of the user in the grid file. 
	 *  The function contains the synchronized statement in the declaration to
	 *   avoid multiple threads(user writing the file at the same time)
	 * - input:
	 * 		-epoch: user's epoch.
	 * 		-userID: Id of the user that want to writes its position in the file
	 * 		-pos: position of the user
	 * 		-port: port where user receive proof loc request
	 *  
	 * ************************************************************************************/
	public static synchronized void update_user_pos(int userID, Point2D pos, int epoch, int port) {
		try {
			File pos_file = new File(pos_file_path);
			String string_to_write = userID + ", " + epoch + ", " + pos.getX() + ", " + pos.getY() + ", " + port+ "\n";
			System.out.println("user ID = "+ userID + "writting his file");
			write(pos_file, string_to_write);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void write(File file, String s) throws IOException {
		FileWriter fw = new FileWriter(file, true);
		fw.write(s);
		fw.close();
	}
	
	
	public static void updateUsersEpoch(int epoch) {
		for(User u: users) {
			u.updateEpoc(epoch);
		}
	}
	
	
	/**************************************************************************************
	 * 										-getAllUsersInEpoch()
	 * - read all user location in the grid file and return them in a list.
	 * - input:
	 * 		-epoch: epoch of the user
	 * 
	 * -returns: List of user location
	 *  
	 * ************************************************************************************/
	public static synchronized List<UserLocation> getAllUsersInEpoch(int epoch) throws IOException {
		File file = new File(pos_file_path);
		List<UserLocation> locations = new ArrayList<>();
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line = br.readLine()) != null) {
			String[] file_content = line.split(", ");
			int line_epoc = Integer.parseInt(file_content[1]);
			if(epoch == line_epoc) {
				int userId = Integer.parseInt(file_content[0]);
				int X = Integer.parseInt(file_content[2]);
				int Y = Integer.parseInt(file_content[3]);
				int port = Integer.parseInt(file_content[4]);
				Point2D position = new Point2D(X,Y);
				UserLocation ul = new UserLocation(userId, epoch, position, port);
				locations.add(ul);
			}
		}
		br.close();
		fr.close();
		return locations;
	}

	public static synchronized int getCurrentEpoch() {
		return currentEpoch;
	}

	public static synchronized int getServerPort() {
		return serverPort;
	}
	
	
	
	/**************************************************************************************
	 * 										-main()
	 * - starts the system. 
	 * - input: 
	 * 		-num_user: number of the user to start.
	 * 		-G_width: the width of the grid
	 * 		-G_height: the height of the grid
	 * 		-server_port: the port where user will operate
	 *  
	 *  NOTE: to pass argument to a main function in eclipse go to:
	 *  -run -> run configuration -> Arguments
	 *  
	 * ************************************************************************************/
	public static void main(String args[]) {
		int num_users, G_width, G_height, server_port;
		try {
			num_users = Integer.parseInt(args[0]);
			G_width = Integer.parseInt(args[1]);
			G_height = Integer.parseInt(args[2]);
		    server_port = Integer.parseInt(args[3]);
		    
		    
			start_users(num_users, G_width, G_height);
			
			// start server
			
			
			//update user's epoch
			while(true) {
				Thread.sleep(15000);
				currentEpoch++;
				updateUsersEpoch(currentEpoch);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
}
