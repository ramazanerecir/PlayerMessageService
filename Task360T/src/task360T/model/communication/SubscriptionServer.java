package task360T.model.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import task360T.model.communication.entity.Telegram;
import task360T.model.communication.entity.TelegramMessage;

/**
 * Server socket to broadcast messages
 * 
 * */
public class SubscriptionServer extends Thread
{
	private static final Logger LOG = Logger.getLogger(SubscriptionServer.class);

	private static SubscriptionServer instance = null;
	private ServerSocket listenerSocket = null;
	private boolean running = false;

	private SubscriptionServer()
	{
		super();
	}

	@Override
	public void run()
	{
		LOG.debug("Running Subscription Server");
		
		while (this.running)
		{
			try
			{
				Socket clientSocket = this.listenerSocket.accept();
				if (clientSocket != null)
				{
					SubscriptionClient client = new SubscriptionClient(clientSocket);
					client.start();
				}
			}
			catch (Exception e)
			{
				/* 
				 * If an exception is thrown here, it means that initializing a client connection failed. 
				 * It is OK to use a blank catch block here since client will throw an exception as well.
				 */
			}
		}
	}

	/**
	 * Broadcasting telegram message instance
	 * 
	 * */
	public void broadcast(TelegramMessage telegramMessage)
	{
		if (telegramMessage == null)
			return;
		
		if(!SessionManager.isAuthenticated(telegramMessage.getIdentity()))
		{
			LOG.warn(MessageFormat.format("Unauthenticated Messenger identity {0} is not allowed to send message", telegramMessage.getIdentity()));
			return;
		}

		broadcast(telegramMessage.toTelegram());
	}

	/**
	 * Broadcasting telegram to the subscribers which are connected to socket server
	 * Telegram is the basic message which is converted from TelegramMessage instance
	 * 
	 * */
	private void broadcast(Telegram telegram)
	{
		for (Entry<String, SubscriptionClient> entry : SessionManager.getSubscriberMap().entrySet())
		{
			String sessionId = entry.getKey();
			SubscriptionClient client = entry.getValue();

			try
			{
				client.pushBroadcastTelegram(telegram);
			}
			catch (InterruptedException e)
			{
				LOG.error("Cannot broadcast telegram to client with session " + sessionId, e);
				try
				{
					LOG.info("Disconnecting client.");
					client.closeConnection();
				}
				catch (Exception e2)
				{
					LOG.error("Cannot disconnect client.", e2);
				}
			}
		}
	}

	/**
	 * Initializing socket server
	 * 
	 * */
	public void init(int portNumber) throws IOException
	{
		this.running = true;
		this.listenerSocket = new ServerSocket(portNumber);
	}

	/**
	 * Stops connection listener thread and closes socket.
	 * 
	 */
	public void shutdown() throws IOException
	{
		this.running = false;

		if (this.listenerSocket != null)
		{
			this.listenerSocket.close();
		}

		LOG.debug("Subscription Server closed");
	}

	/**
	 * Get the static instance of this class.
	 * 
	 */
	public static SubscriptionServer getInstance()
	{
		if (instance == null)
		{
			instance = new SubscriptionServer();
		}

		return instance;
	}
}