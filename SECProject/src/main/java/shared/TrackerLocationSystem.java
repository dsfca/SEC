package shared;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.ini4j.Ini;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.Server;
import crypto.AESProvider;
import crypto.RSAProvider;
import user.NormalUser;
import user.UserLocation;

public class TrackerLocationSystem {
	public static final String password = "SEC";
	
	private  List<NormalUser> users = new ArrayList<NormalUser>();
	private static final String pos_file_path = "resources/Grid.txt";
	private int num_users;
	private int G_width;
	private int G_height;
	private int server_start_port;
	private  int NUM_BIZANTINE_USERS;
	private int NUM_BIZANTINE_SERVERS;
	private int num_servers;
	
	private static TrackerLocationSystem INSTANCE;

	public TrackerLocationSystem( int G_width, int G_height) throws Exception {
		INSTANCE = this;
		this.num_users =  new Ini(new File("variables.ini")).get("UserSpecs","number_of_users", Integer.class);;
		this.G_width = G_width;
		this.G_height = G_height;
		this.server_start_port = new Ini(new File("variables.ini")).get("Server","server_start_port", Integer.class);
		this.NUM_BIZANTINE_SERVERS = new Ini(new File("variables.ini")).get("Server","number_of_byzantines", Integer.class);
		this.NUM_BIZANTINE_USERS = new Ini(new File("variables.ini")).get("UserSpecs","number_of_byzantines", Integer.class);
		this.num_servers = new Ini(new File("variables.ini")).get("Server","number_of_servers", Integer.class);
		verifyParameters();
	}
	
	
	public static TrackerLocationSystem getInstance() {
		if(INSTANCE == null)
			try {
				INSTANCE = new TrackerLocationSystem( 1, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return INSTANCE;
	}
	
	public void start() throws Exception {
		start_servers();
		ini_pos_file(num_users, 10, G_width, G_height);
		start_users(num_users, G_width, G_height);
	}
	
	public void start_servers() throws Exception {
		int j = 0;
		boolean isByzantine = false;
		for(int i = 0; i < num_servers; i++) {
			if(j < NUM_BIZANTINE_SERVERS) {
				isByzantine = true;
				j++;
			}
			else
				isByzantine = false;
			Server server = new Server(i, (server_start_port + i), isByzantine);
			server.init();
		}
	}
	public void start_users(int num_users, int g_width, int g_height) throws Exception {
		for(int i = 0; i < num_users; i++) {
			NormalUser user = new NormalUser(i);
			users.add(user);
		}
	}
	
	public int getNumBizantineServers() {
		return this.NUM_BIZANTINE_SERVERS;
	}
	
	public int getNumBizantineUsers() {
		return this.NUM_BIZANTINE_USERS;
	}
	
	public int getNumServers() {
		return this.num_servers;
	}
	
	public int getMyServerPort(int myServerID) {
		return server_start_port + myServerID;
	}
	
	private void verifyParameters() throws Exception {
		if(this.getNumServers() <=  3*this.getNumBizantineServers())
			throw new Exception("number of server must be 3 times bigger than number of bizantine servers");
		else if(this.num_users <= 3*this.getNumBizantineUsers())
			throw new Exception("number of users must be 3 times bigger than number of bizantine users");
		else if(this.G_height < 0 || this.G_width < 0)
			throw new Exception("non negative dimensions");
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
	public static synchronized void update_user_pos(int userID, Point2D pos, int epoch, int port, boolean append) {
		try {
			File pos_file = new File(pos_file_path);
			String string_to_write = userID + ", " + epoch + ", " + pos.getX() + ", " + pos.getY() + ", " + port+ "\n";
			write(pos_file, string_to_write, append);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void write(File file, String s, boolean append) throws IOException {
		FileWriter fw = new FileWriter(file, append);
		fw.write(s);
		fw.close();
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

	
	/**************************************************************************************
	 * 										-getUserPublicKey()
	 * - returns public key of the user with given id. the public key must be
	 *  generated previously in the path resources/public_keys/user" + id +"_public.key where the id is the id of the user.
	 *   other wise the exception will raise
	 * - input:
	 * 		-n_user: number of user to start the system
	 * 		-n_epoch: number of epochs
	 * 		-G_width: width of the grid
	 * 		-G_height: height of the grid.
	 * 
	 * -returns: void
	 *  
	 * ************************************************************************************/
	public  PublicKey getUserPublicKey(int id, String type) throws Exception {
		PublicKey key = null;
		String path = "resources/public_keys/"+type+"" + id +"_public.key";
		//key = RSAProvider.readPubKey(path);
		key = RSAProvider.readpublicKeyFromFile(path);
		return key;
	}
	
	public  PublicKey getServerPublicKey(int serverID) throws Exception {
		PublicKey key = null;
		String path = "resources/public_keys/server" + serverID +"_public.key";
		//key = RSAProvider.readPubKey(path);
		key = RSAProvider.readpublicKeyFromFile(path);
		return key;
	}
	
	
	/**************************************************************************************
	 * 										-ini_pos_file()
	 * - init the position of the user in the file grid, in this case the users don't have
	 *  to update their position at each epoch because it's already in the file
	 * - input:
	 * 		-n_user: number of user to start the system
	 * 		-n_epoch: number of epochs
	 * 		-G_width: width of the grid
	 * 		-G_height: height of the grid.
	 * 
	 * -returns: void
	 *  
	 * ************************************************************************************/
	public  void ini_pos_file(int n_user, int n_epoch, int G_width, int G_height) {
		boolean append = false;
		for(int i = 1; i <= n_epoch; i++) {
			for(int j = 0; j < n_user; j++) {
				Point2D userPos = new Point2D((int)(Math.random()*G_width), (int)(Math.random()*G_height));
				if(i == 1 && j == 1) { append = false;}
				else {	 append = true; }
				update_user_pos(j, userPos, i, 9090 + j, append);
			}
		}
			
	}
	
	/**************************************************************************************
	 * 										-getPosInEpoc():
	 * - return the user location at a given epoch
	 * -input
	 * 		- myID: the Id of the user that want to get its position. 
	 * 		- epoch: the epoch where he wants to get its position
	 * 		
	 * - return: UserLocation
	 * 
	 * ************************************************************************************/
	public static UserLocation getPosInEpoc(int myId, int epoch) {
		try {
			List<UserLocation> users = TrackerLocationSystem.getAllUsersInEpoch(epoch);
			for(UserLocation u: users) {
				if(u.getUserId() == myId && epoch == u.getEpoch())
					return u;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public  String getDHkeySigned(PublicKey dfPubKey, String privateKeyPath) throws Exception {
		byte[] myDHpbkbytes = dfPubKey.getEncoded();
		String pbkB64 = Base64.getEncoder().encodeToString(myDHpbkbytes);
		PrivateKey privkey = RSAProvider.readprivateKeyFromFile(privateKeyPath, password);
		String digSigMyDHpubkey = RSAProvider.getTexthashEnWithPriKey(pbkB64, privkey);
		return digSigMyDHpubkey;
	}
	
	public  Key createSecretKey(DiffieHelman df, String DHpubkeyDigSig, String DHpubKey, PublicKey key) throws Exception {
		boolean isServDHPbkDigSigValid = RSAProvider.istextAuthentic(DHpubKey, DHpubkeyDigSig, key);
		if(isServDHPbkDigSigValid) {
			byte[] serverPbkContent = Base64.getDecoder().decode(DHpubKey);
			PublicKey serverDHPubKey = DiffieHelman.generatePublicKey(serverPbkContent);
			return df.agreeSecretKey(serverDHPubKey, true);
		}
		else 
			throw new IllegalArgumentException("digital signature of the server key is not valid");
	}
	
	
	public  JsonObject getSecureText(Key key, PrivateKey prvkey, String plaintext) throws Exception {
		String cipherText = AESProvider.getCipherOfPlainText(plaintext, key);
		String textDigSig = RSAProvider.getTexthashEnWithPriKey(plaintext, prvkey);
		JsonObject secureText = JsonParser.parseString("{}").getAsJsonObject();
		{
			secureText.addProperty("ciphertext",cipherText);
			secureText.addProperty("textDigitalSignature", textDigSig);
		}
		return secureText;
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
		int G_width, G_height;
		try {
			//num_users = Integer.parseInt(args[0]);
			G_width = Integer.parseInt(args[0]);
			G_height = Integer.parseInt(args[1]);
			TrackerLocationSystem trl = new TrackerLocationSystem(G_width, G_height);
			trl.start();
			/*while(true) {
				Thread.sleep(10000);
				int id = (int)(Math.random()*num_users);
				int epoch = (int)(Math.random()*10);
				System.out.println("user with ID = " + id + " proving its location on epoch = "+ epoch);
				User u = trl.getUsers().get(id);
				subLocRepReply serverReply = u.proveLocation(epoch);
				System.out.println("user ID = "+id+", server code: "+ serverReply.getReplycode() + ", server message:"+ serverReply.getReplymessage());
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}

}