package task360T.model.communication.entity;

/**
 * Empty Telegram Message which is sent for response
 * Empty Telegram Message is also used to finalize SubscriptionClient's LinkedBlockingQueue
 * */
public class VoidTelegram implements TelegramMessage
{
	public VoidTelegram()
	{
		super();
	}

	@Override
	public Telegram toTelegram()
	{
		return Telegram.build(VoidTelegram.class);
	}

	@Override
	public void loadFromTelegram(Telegram telegram)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String getIdentity() 
	{	
		return "";
	}
	
	

}
