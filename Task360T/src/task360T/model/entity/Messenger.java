package task360T.model.entity;

import task360T.model.communication.entity.TelegramMessage;

/**
 * Messenger Interface should be implemented by the classes who wants to send/receive messages
 * */
public interface Messenger 
{
	public void setActive(boolean active);
	public boolean getActive();
	
	public int getNumOfReceived();
	public int getNumOfSent();
	
	public void setCredentials(boolean status);
	public Credentials getCredentials();
	
	public void sendMessage();
	public void receiveMessage(TelegramMessage telegramMessage);
}
