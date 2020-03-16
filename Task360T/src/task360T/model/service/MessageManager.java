package task360T.model.service;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import task360T.model.communication.SessionManager;
import task360T.model.communication.entity.TelegramMessage;
import task360T.model.entity.Credentials;
import task360T.model.entity.Messenger;

/*
 * MessageManager authenticates the Messenger instances to the system.
 * It also distributes the received messages to authenticated Messanger instances.
 * 
 * */
public class MessageManager 
{
	private static final Logger LOG = Logger.getLogger(MessageManager.class);
	
	private static MessageManager instance;
	
	private Queue<Messenger> messengerList = new ConcurrentLinkedQueue<Messenger>();
	
	private MessageManager()
	{
		//
	}
	
	public static MessageManager getInstance()
	{
		if(instance == null)
		{
			instance = new MessageManager();
		}
		
		return instance;
	}
	
	public Queue<Messenger> getMessengerList() 
	{
		return messengerList;
	}
	
	/*
	 * Register and authenticate a messenger
	 * 
	 */
	public void registerMessenger(Messenger messenger)
	{
		LOG.debug(MessageFormat.format("Registering messenger {0}", messenger.getCredentials().getUsername()));
		//authenticate
		messenger.getCredentials().setIdentity(SessionManager.buildSessionId());
		
		Credentials responseCredentials = SessionManager.authenticate(messenger.getCredentials());
		
		messenger.getCredentials().setAuthenticated(responseCredentials.isAuthenticated());
		
		if(messenger.getCredentials().isAuthenticated())
		{
			messenger.setActive(true);
			messengerList.add(messenger);
			LOG.debug(MessageFormat.format("Registered messenger {0}", messenger.getCredentials()));
		}
		else
		{
			LOG.debug(MessageFormat.format("Failed to authenticate and register messenger {0}",
					messenger.getCredentials().getUsername()));
		}
	}
	
	/*
	 * Received messages are distributed to the messengers in the list.
	 * Message will not be distributed to Messenger who is not active or who sent message.
	 * 
	 */
	public void receiveMessage(TelegramMessage telegramMessage)
	{
		messengerList.stream().filter(m -> m.getActive() &&
				!Objects.equals(telegramMessage.getIdentity(), m.getCredentials().getIdentity()))
			.forEach(messenger -> messenger.receiveMessage(telegramMessage));
	}

}
