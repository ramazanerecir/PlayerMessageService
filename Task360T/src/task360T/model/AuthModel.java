package task360T.model;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

/**
 * Authentication Model
 * Each Player instance will be authenticated by the system
 * 
 * */
public class AuthModel 
{
	private static final Logger LOG = Logger.getLogger(AuthModel.class);

	private static AuthModel instance;

	public AuthModel()
	{
		super();
	}

	/**
	 * Default authentication method
	 */
	public boolean authenticate(String username, String identity)
	{
		LOG.debug(MessageFormat.format("Authenticating user: {0} with identity: {1}", username, identity));
		return true;
	}
	
	public static AuthModel getInstance()
	{
		if (instance == null)
		{
			instance = new AuthModel();
		}

		return instance;
	}

}
