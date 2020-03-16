package task360T.model.service;

import java.io.IOException;
import org.apache.log4j.Logger;

import task360T.model.communication.ListenerClient;
import task360T.model.communication.SessionManager;
import task360T.model.communication.SubscriptionServer;
import task360T.model.service.enumtype.ServiceStatus;

/*
 * Recovery Service initializes and closes Socket Server and Client sessions
 * In case any failure, it tries to recover connections
 * 
 * */
public class RecoveryService extends Thread
{
	private static final Logger LOG = Logger.getLogger(RecoveryService.class);
	
	private static RecoveryService instance;
	private boolean running = false;
	
	private RecoveryService()
	{
		//
	}
	
	public static RecoveryService getInstance()
	{
		if(instance == null)
		{
			instance = new RecoveryService();
		}
		
		return instance;
	}
	
	@Override
	public void run()
	{
		while (this.running) 
		{
			try {
				switch (ApplicationService.getStatus()) {
					case STATUS_OK:
						Thread.sleep(100);
						break;
						
					case CLOSED:
						this.running = false;
						break;
						
					case CLOSING:
						Thread.sleep(10);
						break;
		
					case INITIALIZING:
						LOG.debug("Initializing Application Service");
						RecoveryService.getInstance().recover(true);
						LOG.debug("Application Service initializing is completed");
						break;
						
					default:
						Thread.sleep(1000);
						LOG.debug("Recover is started");
						RecoveryService.getInstance().recover(false);
						LOG.debug("Application Service recovery is completed");
						break;
				}
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	public void init()
	{
		this.running = true;
	}
	
	private void initSubscriptionServer() throws Exception
	{
		LOG.debug("Subscription Server init requested.");
		SubscriptionServer.getInstance().init(AppData.TCP_SERVER_SUBSCRIPTION_PORT_NUMBER);
		SubscriptionServer.getInstance().start();
		LOG.debug("Subscription Server init succeeded.");
	}
	
	private void initListenerClient()
	{
		LOG.debug("Listener Client init requested.");
		ListenerClient.getInstance().init(AppData.TCP_CLIENT_LISTEN_HOST_NAME, 
				AppData.TCP_CLIENT_LISTEN_PORT_NUMBER);
		ListenerClient.getInstance().start();
		LOG.debug("Listener Client succeeded.");
	}
	
	private void recover(boolean initializing)
	{
		if(initializing || 
				ApplicationService.getStatus() == ServiceStatus.SUBSCRIPTION_SERVER_INIT_FAILED)
		{
			try
			{
				initSubscriptionServer();
			}
			catch (Exception e)
			{
				LOG.error("Subscription Server Service init failed.", e);
				ApplicationService.setStatus(ServiceStatus.SUBSCRIPTION_SERVER_INIT_FAILED);
				throw new RuntimeException("Subscription Server Service init failed.", e);
			}
		}
		
		if(initializing || 
				ApplicationService.getStatus() == ServiceStatus.LISTENER_CLIENT_INIT_FAILED)
		{
			try 
			{
				initListenerClient();
			} 
			catch (Exception e) 
			{
				LOG.debug("Listener Client Service init failed.", e);
				ApplicationService.setStatus(ServiceStatus.LISTENER_CLIENT_INIT_FAILED);
				throw new RuntimeException("Listener Client Service init failed.", e);
			}
		}
		
		ApplicationService.setStatus(ServiceStatus.STATUS_OK);
	}
	
	public void close()
	{
		LOG.debug("Sessions will be closed");
		this.running = false;
		ApplicationService.setStatus(ServiceStatus.CLOSING);

		SessionManager.getListenerMap().forEach((key, value) -> SessionManager.closeSession(key));
		SessionManager.getSubscriberMap().forEach((key, value) -> SessionManager.closeSession(key));
		SessionManager.getAuthMap().forEach((key, value) -> SessionManager.closeSession(key));
		
		try {
			SubscriptionServer.getInstance().shutdown();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
		ApplicationService.setStatus(ServiceStatus.CLOSED);
		LOG.debug("Sessions are closed");
	}
}
