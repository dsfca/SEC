package server;

import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.server.grpc.ServerService.Position;

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
	static MongoCollection<Document> mongo_collection;
	

	
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
	
	public String getCollection() {
		return ini.get("Mongo","mongo_collection", String.class);
	}
	
	public void connectToMongo() {
		mongo_client = new MongoClient(new MongoClientURI(getHost()));
		mongo_db = mongo_client.getDatabase(getDatabase());
		mongo_collection = mongo_db.getCollection(getCollection());
		//System.out.println(mongo_db.getName());
	}

	/**
	 * Check username in database
	 * @param username
	 * @return
	 */
	public boolean existsUsername(String username) {
		MongoCursor<Document> cursor = mongo_collection.find().iterator();

		while (cursor.hasNext()) {
			if(cursor.next().get("username").equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void addReportToDatabase(String user, int epoch, int x, int y) {
		 Document document = new Document();
		 document.append("user", user);
		 document.append("epoch",epoch);
		 String position = x + ", " + y;
		 document.append("position", position);
		 mongo_collection.insertOne(document);
	}
	
	/**
	 * Get location documents given a specific epoch
	 * @param epoch
	 * @return
	 */
	public ArrayList<String> getLocationsGivenEpoch(int epoch) {
		MongoCursor<Document> cursor = mongo_collection.find().iterator();
		ArrayList <String> locations = new ArrayList <String> ();
		
		while (cursor.hasNext()) {
			Document current  = cursor.next();
			if(current.get("epoch").equals(epoch)) {
				System.out.println(current.toString());
				locations.add(current.toString());
			}
		}
		return locations;
	}
	
	public void cleanDatabase() {
		mongo_collection.drop();
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		InteractWithDB it = new InteractWithDB("conn_mongo.ini");
		it.cleanDatabase();
		it.addReportToDatabase("user1", 0, 0, 0);
		ArrayList <String> locations = it.getLocationsGivenEpoch(0);
	}

}
