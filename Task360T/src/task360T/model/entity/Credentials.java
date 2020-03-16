package task360T.model.entity;

import java.io.Serializable;
import java.text.MessageFormat;

/*
 * Player credentials having username and an unique identity for authentication
 * 
 * */
public class Credentials implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4676299656609042082L;

	private String username;
	private String identity;
	private boolean authenticated;
	
	public Credentials()
	{
		super();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	@Override	
	public String toString()
	{
		return MessageFormat.format("Player : {0} - Identity : {1}", username, identity);
	}
}
