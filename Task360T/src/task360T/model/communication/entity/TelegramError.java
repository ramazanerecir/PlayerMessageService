package task360T.model.communication.entity;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

/**
 * TelegramError class. 
 * In case any problem occurred during processing received/listened message
 * A TelegramError message is sent to report rejected message to sender
 * */
public class TelegramError implements Serializable, TelegramMessage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 601252026188220305L;
	
	private String rejectedTelegram;

	public TelegramError()
	{
		super();
	}

	public String getRejectedTelegram()
	{
		return rejectedTelegram;
	}

	public void setRejectedTelegram(String rejectedTelegram)
	{
		this.rejectedTelegram = rejectedTelegram;
	}

	@Override
	public Telegram toTelegram()
	{
		String encodedString = new String(Base64.encodeBase64(rejectedTelegram.getBytes()));

		Telegram telegram = Telegram.build(TelegramError.class);
		telegram.set("rejectedTelegramBase64Encoded", encodedString);

		return telegram;
	}

	@Override
	public void loadFromTelegram(Telegram telegram)
	{

	}
	
	@Override
	public String getIdentity()
	{
		return "";
	}

}
