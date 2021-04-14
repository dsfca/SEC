package server;

import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.server.grpc.ServerService.Position;

import shared.Point2D;

import org.bson.Document;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



public class InteractWithDB {
	
	//Mongo
	static Ini ini;
	static MongoDatabase mongo_db;
	static MongoClient mongo_client;
	static MongoCollection<Document> mongo_collection_all;
	static MongoCollection<Document> mongo_collection_validated;
	

	
	public InteractWithDB(String mongo_ini) throws FileNotFoundException, IOException {
		InteractWithDB.ini = new Ini(new File(mongo_ini));
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
	
	
	
	public void connectToMongo() {
		mongo_client = new MongoClient(new MongoClientURI(getHost()));
		mongo_db = mongo_client.getDatabase(getDatabase());
		mongo_collection_all = mongo_db.getCollection(getAllCollection());
		mongo_collection_validated = mongo_db.getCollection(getValidatedCollection());
		//System.out.println(mongo_db.getName());
	}

	/**
	 * Check username in database
	 * @param username
	 * @return
	 */
	/*public boolean existsUsername(String username) {
		MongoCursor<Document> cursor = mongo_collection_validated.find().iterator();

		while (cursor.hasNext()) {
			if(cursor.next().get("username").equals(username)) {
				return true;
			}
		}
		return false;
	}*/
	
	//ADDS A REPORT TO COLLECTION "ALL_LOCATIONS"
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
	
	/**
	 * Get location document given a specific epoch
	 * @param epoch
	 * @return
	 */
	public Point2D getLocationsGivenEpoch(int user, int epoch) {
		MongoCursor<Document> cursor = mongo_collection_validated.find().iterator();
		Point2D location = null;
		
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			if(current.get("epoch").equals(epoch) && current.get("user").equals(user)) {
				System.out.println(current.toString());
				String location_string = current.get("location").toString();
				location = new Point2D(Integer.valueOf(location_string.substring(1, 2)), Integer.valueOf(location_string.substring(3, 4)));
			}
		}
		return location;
	}
	
	//ERASES COLLECTION "VALIDATED" CONTENT
	public void cleanValidatedCollection() {
		mongo_collection_validated.drop();
	}
	
	//ERASES COLLECTION "ALL_LOCATIONS" CONTENT
		public void cleanAllLocationsCollection() {
			mongo_collection_all.drop();
		}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		InteractWithDB it = new InteractWithDB("variables.ini");
		it.cleanValidatedCollection();
		it.cleanAllLocationsCollection();
		//it.addReportToDatabase("user1", 0, 0, 0);
		//ArrayList <String> locations = it.getLocationsGivenEpoch(0);
	}

}
