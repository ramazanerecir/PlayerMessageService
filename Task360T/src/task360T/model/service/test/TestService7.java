package task360T.model.service.test;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import task360T.model.entity.Messenger;
import task360T.model.entity.Player;
import task360T.model.service.ApplicationService;
import task360T.model.service.MessageManager;
import task360T.model.service.enumtype.ServiceStatus;

/*
 * Task at step 7: additional challenge (nice to have) opposite to 5: have every player in a separate JAVA process (different PID).
 * 
 * */
public class TestService7 extends Thread implements TestService
{
	private static final Logger LOG = Logger.getLogger(TestService7.class);

	private boolean running = false;
	private boolean completed = false;
	
	private boolean isInitiator;
	
	
	public TestService7(boolean isInitiator)
	{
		super();
		
		this.isInitiator = isInitiator;
		init();
	}
	
	private void init()
	{
		this.running = true;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public void run()
	{
		LOG.info(MessageFormat.format("TestService7 - {0} - is started", isInitiator ? "Initiator" : "Receiver"));
		
		Player player = null;
		if(isInitiator)
		{
			player = new Player("Initiator Player", true);
			MessageManager.getInstance().registerMessenger(player);
			player.sendMessage();
		}
		else
		{
			player = new Player("Receiver Player", false);
			MessageManager.getInstance().registerMessenger(player);
		}
		
		while (this.running) 
		{
			boolean allFinished = true;
			
			for(Messenger messenger : MessageManager.getInstance().getMessengerList())
			{
				if(messenger.getNumOfSent() >= 10 && 
						messenger.getNumOfReceived() >= 10)
				{
					messenger.setActive(false);
				}
				else
				{
					allFinished = false;
				}
			}
			
			completed = allFinished;
			
			if(completed)
			{
				this.running = false;
			}
		}

		LOG.info(MessageFormat.format("TestService7 - {0} - is completed", isInitiator ? "Initiator" : "Receiver"));
		
		//It will be easy to check from System out, so summary is also written to output
		System.out.println(player.logSummary());
	}
}
