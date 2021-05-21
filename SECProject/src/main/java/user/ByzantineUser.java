  
package user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ByzantineUser {

	List<String> submittedProves = new ArrayList<>();
	
	public void submittProve(String proove) {
		submittedProves.add(proove);
	}
	
	public  String claming_anotherUser() throws Exception {
		ArrayList<String> proves = new ArrayList<>();
			File file = new File("resources/byzantine_test/claming_another_user.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null) {
				proves.add(line);
			}
			br.close();
			int ind = (int) (Math.random()*proves.size());
			return proves.get(ind);
	}
	
	public  String  getEqualSubmit() {
		return submittedProves.get(0);
	}
	
	public static void storeProof(String prooff) throws IOException {
		File file = new File("resources/byzantine_test/claming_another_user.txt");
		FileWriter fw = new FileWriter(file, true);
		fw.write(prooff);
		fw.close();
	}
	
	
	



}