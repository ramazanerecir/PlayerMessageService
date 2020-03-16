package task360T.model.communication.entity;

/**
 * TelegramMessage interface functions
 *
 * */
public interface TelegramMessage
{
	//Converts the TelegramMessage instance to Telegram objects
	public Telegram toTelegram();
	
	//Converts the Telegram object to TelegramMessage instance
	public void loadFromTelegram(Telegram telegram);
	
	//Returns the Telegram Message sender's identity
	public String getIdentity();
}

