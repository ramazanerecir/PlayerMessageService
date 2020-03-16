package task360T.model.communication;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import task360T.model.AuthModel;
import task360T.model.entity.Credentials;

/**
 * SessionManager manages the sessions for subscriber clients, listener clients and authenticated messengers/players
 * 
 * */
public class SessionManager
{
	private static final Logger LOG = Logger.getLogger(SessionManager.class);

	//Socket clients connected to Server Socket that is broadcasting messages
	private static final Map<String, SubscriptionClient> subscriberMap = new ConcurrentHashMap<String, SubscriptionClient>();
	
	//Socket listener clients which receive messages
	private static final Map<String, ListenerClient> listenerMap = new ConcurrentHashMap<String, ListenerClient>();

	//Player authentication status map
	private static final Map<String, Boolean> authMap = new ConcurrentHashMap<String, Boolean>();

	private SessionManager()
	{
		super();
	}

	public static Map<String, SubscriptionClient> getSubscriberMap()
	{
		return subscriberMap;
	}

	public static SubscriptionClient getSubscriptionHandler(String sessionId)
	{
		return subscriberMap.get(sessionId);
	}
	
	public static Map<String, ListenerClient> getListenerMap()
	{
		return listenerMap;
	}

	public static ListenerClient getListenerHandler(String sessionId)
	{
		return listenerMap.get(sessionId);
	}
	
	public static Map<String, Boolean> getAuthMap() {
		return authMap;
	}

	/**
	 * Register a Subscription Client
	 */
	public static String registerSession(SubscriptionClient subscriptionClient)
	{
		String sessionId = buildSessionId();

		subscriberMap.put(sessionId, subscriptionClient);

		LOG.debug("Registered Subscription Client session: " + sessionId);

		return sessionId;
	}
	
	/**
	 * Register a Listener Client
	 */
	public static String registerSession(ListenerClient listenerClient)
	{
		String sessionId = buildSessionId();

		listenerMap.put(sessionId, listenerClient);

		LOG.debug("Registered Listener Client session: " + sessionId);

		return sessionId;
	}

	/**
	 * UnRegister a Subscription Client and remove from broadcasting list
	 */
	public static void unregisterSubscriptionSession(String sessionId)
	{
		subscriberMap.remove(sessionId);
		LOG.debug("Unregistered Subscription Client session: " + sessionId);
	}
	
	/**
	 * UnRegister a Listener Client
	 */
	public static void unregisterListenerSession(String sessionId)
	{
		listenerMap.remove(sessionId);
		LOG.debug("Unregistered Listener Client session: " + sessionId);
	}
	
	/**
	 * Create a Subscription Client session
	 */
	public static String openSession(SubscriptionClient subscriptionClient)
	{
		String sessionId = buildSessionId();
		subscriberMap.put(sessionId, subscriptionClient);
		LOG.debug("Open Subscription Client session: " + sessionId);
		return sessionId;
	}
	
	/**
	 * Create a Listener Client session
	 */
	public static String openSession(ListenerClient listenerClient)
	{
		String sessionId = buildSessionId();
		listenerMap.put(sessionId, listenerClient);
		LOG.debug("Open Listener Client session: " + sessionId);
		return sessionId;
	}
	
	/**
	 * Given unique identity session or player is closed
	 */
	public static void closeSession(String sessionId)
	{
		authMap.remove(sessionId);
	
		SubscriptionClient subscriptionHandler = subscriberMap.get(sessionId);
		if (subscriptionHandler != null)
		{
			subscriptionHandler.closeConnection();

			try
			{
				subscriberMap.remove(sessionId);
			}
			catch (Exception e)
			{
				//
			}
		}
		
		ListenerClient listenerHandler = listenerMap.get(sessionId);
		if (listenerHandler != null)
		{
			listenerHandler.closeConnection();

			try
			{
				subscriberMap.remove(sessionId);
			}
			catch (Exception e)
			{
				//
			}
		}
	}
	
	/**
	 * Creating unique id
	 */
	public static String buildSessionId()
	{
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Check whether player is authenticated
	 */
	public static boolean isAuthenticated(String sessionId)
	{
		Boolean authorized = authMap.get(sessionId);
		return authorized == null ? false : authorized;
	}
	
	/**
	 * Authenticate given credentials
	 */
	public static Credentials authenticate(Credentials credentials)
	{
		String username = credentials.getUsername();
		String identity = credentials.getIdentity();

		boolean status = AuthModel.getInstance().authenticate(username, identity);

		credentials.setAuthenticated(status);

		if (credentials.isAuthenticated())
		{
			authMap.put(identity , true);
		}
		return credentials;
	}
}
