package server;

import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.server.grpc.ServerService.Position;

import io.grpc.ManagedChannel;
import shared.Point2D;
import shared.TrackerLocationSystem;

import org.bson.Document;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**WATCH NOTIFICATIONS BELLOW IN: 
							- getLocationsGivenEpoch()							
*/

public class InteractWithDB {
	
	//Mongo
	static Ini ini;
	static MongoDatabase mongo_db;
	static MongoClient mongo_client;
	static MongoCollection<Document> mongo_collection_all;
	static MongoCollection<Document> mongo_collection_validated;
	

	
	public InteractWithDB(String mongo_ini) throws FileNotFoundException, IOException {
		this.ini = new Ini(new File(mongo_ini));
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
		mongo_db = mongo_client.getDatabase(getDatabase());
		mongo_collection_all = mongo_db.getCollection(getAllCollection());
		mongo_collection_validated = mongo_db.getCollection(getValidatedCollection());
		//System.out.println(mongo_db.getName());
	}

	
	
	//ADDS PROOF TO DATABSE - COLLECTION "ALL_LOCATIONS"
	public void addReportToDatabase(int user1, int user2, Point2D pos1, Point2D pos2, int epoch, boolean near) {
		Document document = new Document();
		document.append("user1", user1);
		document.append("user2", user2);
		document.append("pos1", pos1);
		document.append("pos2", pos2);
		document.append("epoch", epoch);
		document.append("near", near);
		mongo_collection_all.insertOne(document);
	}
	
	
	//INSERTS LOCATION TO COLLECTION "VALIDATED"
	public void addLocationToValidated(int user, Point2D pos, int epoch) {
		Document document = new Document();
		document.append("user", user);
		document.append("location", pos.toString());
		document.append("epoch", epoch);
		mongo_collection_validated.insertOne(document);
	}
	
		
	//IF RETURN "NULL", PLACE EXCEPTION (PRINT TO SCREEN) IN CLASS SERVER THAT THE USER DOESNT HAVE A LOCATION PROOF FOR THAT EPOCH
	/**
	 * Get location document given a specific user and epoch (USER->SERVER) - obtainLocationReport()
	 * @param location
	 * @return
	 */
	public Point2D getLocationGivenEpoch(int user, int epoch) {
		MongoCursor<Document> cursor = mongo_collection_validated.find().iterator();
		Point2D location = null;
		
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			if(current.get("epoch").equals(epoch) && current.get("user").equals(user)) {
				String location_string = current.get("location").toString();
				location = new Point2D(Integer.valueOf(location_string.substring(1, 2)), Integer.valueOf(location_string.substring(4, 5)));
			}
		}
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
	

	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		InteractWithDB it = new InteractWithDB("variables.ini");
		it.cleanValidatedCollection();
		it.cleanAllLocationsCollection();
		//LOCATIONS
		it.addLocationToValidated(1, new Point2D(0,0), 1);
		it.addLocationToValidated(4, new Point2D(0,0), 1);
		it.addLocationToValidated(2, new Point2D(0,1), 1);
		
		//Point2D location = it.getLocationGivenEpoch(1, 2);
		//System.out.println(location);
		ArrayList <String> users = it.getUsersGivenPosAndEpoch(new Point2D(0,0), 1);
	}

}
