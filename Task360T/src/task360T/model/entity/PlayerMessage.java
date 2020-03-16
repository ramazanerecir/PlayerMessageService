package task360T.model.entity;

import java.io.Serializable;

import task360T.model.communication.entity.Telegram;
import task360T.model.communication.entity.TelegramMessage;

/**
 * PlayerMessage which is sent/received through Player instances
 * 
 */
public class PlayerMessage implements Serializable, TelegramMessage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 681684371434202834L;
	

	//player identity who sent message
	private String identity;
	
	//message context
	private String message;
		
	//concatenated previous received messages
	private String history;
	
	//Number of sent messages including this one
	private int counter;
	
	public PlayerMessage()
	{
		super();
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	@Override
	public Telegram toTelegram() 
	{
		Telegram telegram = Telegram.build(PlayerMessage.class);

		telegram.set("identity", identity);
		telegram.set("message", message);
		telegram.set("history", history);
		telegram.set("counter", counter);

		return telegram;
	}

	@Override
	public void loadFromTelegram(Telegram telegram) 
	{
		this.identity = telegram.get("identity");
		this.message = telegram.get("message");
		this.history = telegram.get("history");
		this.counter = Integer.valueOf(telegram.get("counter"));
	}

	@Override
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}
}
