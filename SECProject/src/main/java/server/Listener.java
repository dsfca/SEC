package server;

public class Listener {
		private int userID; //user id that listener wants to know its location
		private int epoch;
		private int ListenerPort;
		private String listenerType;
		private int listenerID;
		public Listener(int userID, int epoch, int listenerPort, String listenerType, int listenerID) {
			super();
			this.userID = userID;
			this.epoch = epoch;
			this.ListenerPort = listenerPort;
			this.listenerType = listenerType;
			this.listenerID = listenerID;
		}
		
		
		public int getUserID() {
			return userID;
		}
		public int getEpoch() {
			return epoch;
		}
		public int getListenerPort() {
			return ListenerPort;
		}
		
		public String getListenerType() {
			return listenerType;
		}
		
		public int getListenerID() {
			return listenerID;
		}
		
		
		/**************************************************************************************
		* 											-isListening()
		* -return true if listener is listening for the point of the user id and epoch
		*  
		* 
		* ************************************************************************************/
		public boolean isListening(int userID, int epoch) {
			return this.userID == userID && this.epoch == epoch;  
		}


		public boolean equals(String ListenerType, int listenerID, int userID, int epoch) {
			return this.userID == userID 
					&& this.epoch == epoch &&
					this.listenerID == listenerID
					&& this.listenerType.equals(ListenerType);
		}
		
		
}
