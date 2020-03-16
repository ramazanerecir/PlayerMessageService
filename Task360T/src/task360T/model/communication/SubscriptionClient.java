package task360T.model.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import task360T.model.communication.entity.Telegram;
import task360T.model.communication.entity.TelegramMessage;
import task360T.model.communication.entity.VoidTelegram;

/**
 * Socket client subscribes the subscription server to get broadcasted messages
 * 
 * */
public class SubscriptionClient extends Thread
{
	private static final Logger LOG = Logger.getLogger(SubscriptionClient.class);
	
	private String sessionId;
	private boolean running = false;
	
	private Socket clientSocket = null;
	private BufferedReader reader = null;
	private DataOutputStream writer = null;
	private static final int SO_TIMEOUT = 0;

	private LinkedBlockingQueue<Telegram> telegramQueue;
	
	public SubscriptionClient(Socket clientSocket)
	{
		this.telegramQueue = new LinkedBlockingQueue<>();
		this.clientSocket = clientSocket;
		this.running = true;
		
		try
		{
			this.clientSocket.setSoTimeout(SO_TIMEOUT);
		}
		catch (SocketException e)
		{
			//
		}
	}
	
	/**
	 * Adding telegram message to queue
	 * 
	 * */
	public void pushBroadcastTelegram(Telegram telegram) throws InterruptedException
	{
		push(telegram);
	}

	/**
	 * Adding telegramMessage to queue
	 * 
	 * */
	public void push(TelegramMessage telegramMessage) throws InterruptedException
	{
		if (telegramMessage == null)
			return;

		push(telegramMessage.toTelegram());
	}
	
	/**
	 * Adding telegram message to queue for sending
	 * 
	 * */
	private void push(Telegram telegram) throws InterruptedException
	{
		if (telegram == null)
			return;

		LOG.trace("Adding to queue.");
		this.telegramQueue.put(telegram);
		LOG.trace("Added to queue.");
	}

	@Override
	public void run()
	{
		try
		{
			openConnection();
			sessionId = SessionManager.registerSession(this);
		}
		catch (Exception e)
		{
			LOG.error("Client connection initialization failed.", e);
			closeConnection();
			return;
		}
		
		while (this.running)
		{
			try
			{
				LOG.trace(MessageFormat.format("Waiting for queue session {0}", this.sessionId));
				Telegram telegram = this.telegramQueue.take(); // waits if empty
				LOG.trace(MessageFormat.format("Telegram taken from queue session {0}", this.sessionId));
				
				if(!VoidTelegram.class.getSimpleName().equals(telegram.getTag()))
				{	
					String xmlMessage = TelegramSerializer.getInstance().fastSerialize(telegram);
					LOG.trace(MessageFormat.format("* SENDING *** session {0} *** {1}", this.sessionId, xmlMessage));
					sendMessage(xmlMessage);
				}
			}
			catch (Exception e)
			{
				LOG.error("Client communication interrupted.", e);
				break;
			}
		}
	}
	
	/**
	 * Sending message to client socket who subscribes the Subscription Server socket
	 * 
	 * */
	private void sendMessage(String message)
	{
		try
		{
			this.writer.writeBytes(message + "\r\n");
		}
		catch (IOException e)
		{
			closeConnection();
		}
	}
	
	/**
	 * Creating Subscription reader, writer on socket connection
	 * 
	 */
	private void openConnection() throws IOException
	{
		this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		this.writer = new DataOutputStream(this.clientSocket.getOutputStream());
	}
	
	/**
	 * Closing subscription client connection
	 */
	public void closeConnection()
	{
		this.running = false;
		
		//Adding an empty message in case thread hangs on telegramQueue.take line
		try {
			telegramQueue.put(Telegram.build(VoidTelegram.class));
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		
		try
		{
			this.reader.close();
		}
		catch (IOException e)
		{
			// do nothing
		}

		try
		{
			this.writer.close();
		}
		catch (IOException e)
		{
			// do nothing
		}

		try
		{
			this.clientSocket.close();
		}
		catch (IOException e)
		{
			// do nothing
		}

		try
		{
			if (sessionId != null)
			{
				SessionManager.unregisterSubscriptionSession(sessionId);
			}
		}
		catch (Exception e)
		{
			// do nothing
		}
		
		LOG.debug("Subscription Client closed");
	}
}
