package user;

import shared.Point2D;

public class UserLocation {
	private int userId;
	private int epoch;
	private Point2D position;
	private int port;
	
	public UserLocation(int userId, int epoch, Point2D position, int port) {
		this.userId = userId;
		this.epoch = epoch;
		this.position = position;
		this.port = port;
	}

	public int getUserId() {
		return userId;
	}
	
	public int getEpoch() {
		return epoch;
	}

	public Point2D getPosition() {
		return position;
	}
	
	public int getPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return userId + ", " + epoch + ", " + position.getX() + ", " + position.getY() + ", " + getPort();
	}

	
}
