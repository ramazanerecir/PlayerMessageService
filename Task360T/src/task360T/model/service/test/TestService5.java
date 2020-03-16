package task360T.model.service.test;

import org.apache.log4j.Logger;

import task360T.model.entity.Messenger;
import task360T.model.entity.Player;
import task360T.model.service.ApplicationService;
import task360T.model.service.MessageManager;
import task360T.model.service.enumtype.ServiceStatus;

/*
 * Task at step 5: both players should run in the same java process (strong requirement)
 * 
 * */
public class TestService5 extends Thread implements TestService
{
	private static final Logger LOG = Logger.getLogger(TestService5.class);

	private boolean running = false;
	private boolean completed = false;
	
	public TestService5()
	{
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
		LOG.info("TestService5 is started");
		
		Player initiator = new Player("Initiator Player", true);
		MessageManager.getInstance().registerMessenger(initiator);
				
		Player player2 = new Player("Receiver Player", false);
		MessageManager.getInstance().registerMessenger(player2);
		
		initiator.sendMessage();
		
		while (this.running) 
		{
			boolean allFinished = true;
			
			//Stop Condition in Test Class
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
		
		LOG.info("TestService5 is completed");
		//It will be easy to check from System out, so summary is also written to output
		System.out.println(initiator.logSummary());
		System.out.println(player2.logSummary());
	}
	

}
