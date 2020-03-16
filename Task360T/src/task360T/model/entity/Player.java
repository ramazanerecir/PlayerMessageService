package task360T.model.entity;

import java.text.MessageFormat;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import task360T.model.communication.SubscriptionServer;
import task360T.model.communication.entity.TelegramMessage;

/*
 * Player implemented to send & receive messages
 * */
public class Player implements Messenger
{
	private static final Logger LOG = Logger.getLogger(Player.class);
			
	private Credentials credentials;
	private int messageSent;
	private int messageReceived;
	private String messageHistory;
	
	private boolean active;
	private boolean initiator;
	
	public Player(String username, boolean initiator)
	{
		this.credentials = new Credentials();
		this.credentials.setUsername(username);
		
		this.messageSent = 0;
		this.messageReceived = 0;
		this.messageHistory = "";
		
		this.active = false;
		this.initiator = initiator;
	}
	
	@Override
	public void setActive(boolean active) 
	{
		this.active = active;
	}

	@Override
	public boolean getActive() 
	{
		return this.active;
	}
	
	public boolean isInitiator() {
		return initiator;
	}

	public void setInitiator(boolean initiator) {
		this.initiator = initiator;
	}
	
	public String getMessageHistory() {
		return messageHistory;
	}

	public void setMessageHistory(String messageHistory) {
		this.messageHistory = messageHistory;
	}

	@Override
	public int getNumOfReceived() 
	{
		return messageReceived;
	}

	@Override
	public int getNumOfSent() 
	{
		return messageSent;
	}
	

	@Override
	public void setCredentials(boolean status) 
	{
		this.credentials.setAuthenticated(status);
	}
	
	@Override
	public Credentials getCredentials()
	{
		return this.credentials;
	}

	@Override
	public void sendMessage() 
	{
		if(!getActive())
		{
			//Skipping sending message for the player who reached message limit that is controlled by active field
			LOG.debug(MessageFormat.format("Skipping sending message from user {0} ", getCredentials()));
			return;
		}
		
		LOG.debug(MessageFormat.format("Sending message from user {0} ", getCredentials()));
		
		++messageSent;
		
		PlayerMessage newMessage = new PlayerMessage();
		newMessage.setIdentity(getCredentials().getIdentity());
		newMessage.setMessage(generateMessage());
		newMessage.setHistory(messageHistory);
		newMessage.setCounter(messageSent);
		
		SubscriptionServer.getInstance().broadcast(newMessage);
	}

	@Override
	public void receiveMessage(TelegramMessage telegramMessage) 
	{
		LOG.debug(MessageFormat.format("Receiving message to user {0} ", getCredentials()));
		
		if(telegramMessage instanceof PlayerMessage)
		{
			PlayerMessage playerMessage = (PlayerMessage)telegramMessage;
			
			if(!Objects.equals(playerMessage.getIdentity(), getCredentials().getIdentity()))
			{
				++messageReceived;
				
				messageHistory = StringUtils.isEmpty(messageHistory) ? playerMessage.getMessage() :
					messageHistory + ";" + playerMessage.getMessage();
				
				try {
					//Let give some time to Test Service for checking player's message counts 
					Thread.sleep(10);
				} catch (InterruptedException e) {
					//
				}
				
				sendMessage();
			}
		}
	}
	
	/*
	 * Generating generic message
	 * */
	private String generateMessage()
	{
		if(isInitiator())
			return "Initiator Message";
		else
			return "Receiver Message";
	}
	
	/*
	 * Logging summary of Player actions
	 * */
	public String logSummary()
	{
		StringBuilder summary = new StringBuilder();
		summary.append(this.credentials.toString());
		summary.append("\n");
		summary.append(MessageFormat.format("Message Sent : {0}", messageSent));
		summary.append("\n");
		summary.append(MessageFormat.format("Message Received : {0}", messageReceived));
		summary.append("\n");
		summary.append(MessageFormat.format("Message History : {0}", messageHistory));
		
		LOG.info(summary);
		
		return summary.toString();
	}
}
