package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ini4j.Ini;


public class DealWithProofs extends Thread{
	
	
	private static Ini ini;
	private InteractWithDB it;
	
	
	public DealWithProofs(String variables_ini) throws FileNotFoundException, IOException {
		this.ini = new Ini(new File(variables_ini));
		this.it = new InteractWithDB(variables_ini);
	}
	
	
	public Integer getMaxEpochTime() {
		return ini.get("UserSpecs","max_epoch_time", Integer.class);
	}
	
	
	public Integer getNumberOfUsers() {
		return ini.get("UserSpecs","number_of_users", Integer.class);
	}

	
	@Override
	public void run() {
		int sleep_time = getMaxEpochTime();
		int my_current_epoch = 1;
		int number_of_users = getNumberOfUsers();
		try {
			while(true) {
				Thread.sleep(sleep_time);
				for(int i=0; i<number_of_users; i++) {
					dealWithUser(i, my_current_epoch);
				}

				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void dealWithUser(int user, int epoch) {
		
	}

	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		DealWithProofs dp = new DealWithProofs("variables.ini");
		dp.start();
	}

}
