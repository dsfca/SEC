package server;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import shared.Point2D;

import org.bson.Document;
import org.ini4j.Ini;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/**WATCH NOTIFICATIONS BELLOW IN: 
							- getLocationsGivenEpoch()							
*/

public class InteractWithDB {
	
	//Mongo
	static Ini ini;
	private MongoDatabase mongo_db;
	private MongoClient mongo_client;
	private MongoCollection<Document> mongo_collection_all;
	private MongoCollection<Document> mongo_collection_validated;
	private int server_id;
	

	
	public InteractWithDB(String mongo_ini, int server_id) throws FileNotFoundException, IOException {
		this.ini = new Ini(new File(mongo_ini));
		this.server_id = server_id;
		connectToMongo();
	}
	
	public String getHost() {
		return ini.get("Mongo","mongo_host", String.class);
	}
	
	public String getDatabase() {
		return ini.get("Mongo","mongo_database", String.class);
	}
	
	public String getAllCollection() {
		return ini.get("Mongo","mongo_collection_all", String.class);
	}
	
	public String getValidatedCollection() {
		return ini.get("Mongo","mongo_collection_validated", String.class);
	}
	
	
	//ESTABLISH CONNECTION TO DATABASE
	public void connectToMongo() {
		mongo_client = new MongoClient(new MongoClientURI(getHost()));
		mongo_db = mongo_client.getDatabase(getDatabase()+ "_" + server_id) ;
		mongo_collection_all = mongo_db.getCollection(getAllCollection());
		mongo_collection_validated = mongo_db.getCollection(getValidatedCollection());
		System.out.println("DATABASE: Server "+server_id+" successfully connected to database");
		//System.out.println(mongo_db.getName());
	}

	
	
	//ADDS PROOF TO DATABSE - COLLECTION "ALL_LOCATIONS"
	public void addReportToDatabase(int user1, int user2, Point2D pos1, Point2D pos2, int epoch, boolean near, String proof1, String proof2) {
		Document document = new Document();
		document.append("user1", user1);
		document.append("user2", user2);
		document.append("pos1", pos1.toString());
		document.append("pos2", pos2.toString());
		document.append("epoch", epoch);
		document.append("near", near);
		document.append("Digi_sig_u1", proof1);
		document.append("Digi_sig_u2", proof2);
		mongo_collection_all.insertOne(document);
		System.out.println("DB - Added report: " + document.toString() + " TO DB " + mongo_collection_all.getNamespace() + " OF SERVER"+ server_id + " ");
	}
	
	
	//INSERTS LOCATION TO COLLECTION "VALIDATED"
	public void addLocationToValidated(int user, Point2D pos, int epoch) throws Exception {
		Point2D userPoint = getLocationGivenEpoch(user, epoch);
		if(userPoint == null) {
			Document document = new Document();
			document.append("user", user);
			document.append("location", pos.toString());
			document.append("epoch", epoch);
			mongo_collection_validated.insertOne(document);
		}else throw new Exception("user with ID = "+user+ " already submit report for this epoch");
	}
	
		
	//IF RETURN "NULL", PLACE EXCEPTION (PRINT TO SCREEN) IN CLASS SERVER THAT THE USER DOESNT HAVE A LOCATION PROOF FOR THAT EPOCH
	public Point2D getLocationGivenEpoch(int user, int epoch) {
		MongoCursor<Document> cursor = mongo_collection_validated.find().iterator();
		Point2D location = null;
		
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			if(current.get("epoch").equals(epoch) && current.get("user").equals(user)) {
				String location_string = current.get("location").toString();
				location = new Point2D(Integer.valueOf(location_string.substring(1,2)), Integer.valueOf(location_string.substring(3, 4)));
			}
		}
		System.out.println("Checking server " + this.server_id + " submitted locations");
		return location;
	}
	
	
	//(HA->SERVER) - obtainUsersAtLocation()
	public ArrayList<String> getUsersGivenPosAndEpoch(Point2D pos, int epoch) {
		MongoCursor<Document> cursor = mongo_collection_validated.find().iterator();
		ArrayList <String> users = new ArrayList <String> ();
		
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			if(current.get("location").equals(pos.toString()) && current.get("epoch").equals(epoch)) {
				//System.out.println(current.get("user").toString());
				users.add(current.get("user").toString());
			}
		}
		return users;
	}
	
	//CLEAN COLLECTION "VALIDATED" CONTENT
	public void cleanValidatedCollection() {
		mongo_collection_validated.drop();
	}

	//CLEAN COLLECTION "ALL_LOCATIONS" CONTENT
	public void cleanAllLocationsCollection() {
		mongo_collection_all.drop();
	}
	

	//PHASE 2 - Allows a user to obtain all the proofs that it provided to other users in a range of epochs
	public ArrayList<String> getProofsinEpochs(int user, String [] epochs) {
		MongoCursor<Document> cursor = mongo_collection_all.find().iterator();
		ArrayList <String> proofs = new ArrayList <String> ();
		boolean unique = true;
 
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			for (int i=0; i<epochs.length; i++) {

				if(current.get("user2").equals(user)  && (Integer.parseInt(current.get("epoch").toString()) == Integer.parseInt(epochs[i]))) {

					BasicDBObject conveted_proof = new BasicDBObject(current);
					@SuppressWarnings("unchecked")
					Map <String,String> proof = conveted_proof.toMap();
					proof.remove("_id");
					for (String p: proofs) {
						if(p.equals(proof.toString()))
							unique = false;
					}
					if (unique == true) {
						proofs.add(proof.toString());
					}
				}
			}
		}
		return proofs;
	}
	
	//Check if proof is already in the DB
	public boolean isReportInAll(String message) { //String format expected: "1, 1, (1;0), (0;0), 1, true, XXX, YYY"
		MongoCursor<Document> cursor = mongo_collection_all.find().iterator();
		ArrayList <String> proofs = new ArrayList <String> ();
		boolean repeated = false;

		while (cursor.hasNext()) {
			Document current  = cursor.next();
			String tmp = current.get("user1") + ", " + current.get("user2") + ", " + current.get("pos1") + ", " + 
							current.get("pos2") + ", " + current.get("epoch") + ", " + current.get("near") + ", " +
								current.get("Digi_sig_u1") + ", " + current.get("Digi_sig_u2");

			if (tmp.equals(message)) {
				repeated = true;
			}
		}
		return repeated;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		InteractWithDB it = new InteractWithDB("variables.ini", 1);
		it.cleanValidatedCollection();
		it.cleanAllLocationsCollection();
		//LOCATIONS TO VALIDATED
		/**it.addLocationToValidated(1, new Point2D(0,0), 1);
		it.addLocationToValidated(1, new Point2D(2,0), 2);
		it.addLocationToValidated(1, new Point2D(0,1), 3);
		*/
		
		//LOCATIONS TO ALL
		it.addReportToDatabase(1,1,new Point2D(1,0),new Point2D(0,0), 1, true, "XXX", "YYY");
		it.addReportToDatabase(1,3,new Point2D(1,0),new Point2D(0,0), 3, true, "XXX", "YYY");
		it.addReportToDatabase(4,1,new Point2D(1,0),new Point2D(0,0), 6, true, "XXX", "YYY");

		//Point2D location = it.getLocationGivenEpoch(1, 2);
		/**String [] epochs = {"1","3","6"};
		ArrayList <String> users = it.getUsersGivenPosAndEpoch(new Point2D(0,0), 1);
		ArrayList <String> proofs = it.getProofsinEpochs(1, epochs);
		System.out.println(proofs.toString());
		
		Hashtable<Integer, Hashtable<Key, Integer>> sharedKey = new Hashtable<>();
		System.out.println(sharedKey.contains("x"));**/
		it.isReportInAll("1, 1, (1;0), (0;0), 1, true, XXX, YYY");
	}

}