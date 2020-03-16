package task360T.model.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import task360T.model.communication.entity.Telegram;
import task360T.model.communication.entity.TelegramError;
import task360T.model.communication.entity.VoidTelegram;
import task360T.model.entity.PlayerMessage;
import task360T.model.service.ApplicationService;
import task360T.model.service.MessageManager;
import task360T.model.service.enumtype.ServiceStatus;

/**
 * Socket Listener Client which will listen a host:port to read messages and then it processes received messages
 * Processed messages will be sent to MessageManager to be distributed to Players
 * 
 * */
public class ListenerClient extends Thread
{
	private static final Logger LOG = Logger.getLogger(ListenerClient.class);

	private static ListenerClient instance = null;
	private Socket socket = null;
	private BufferedReader reader = null;
	private DataOutputStream writer = null;
	private static final int SO_TIMEOUT = 0;
	
	private String sessionId;
	private boolean running;
	
	private ListenerClient()
	{		
		super();
	}
	
	public static ListenerClient getInstance()
	{
		if (instance == null)
		{
			instance = new ListenerClient();
		}
		return instance;
	}
	
	/**
	 * Creating Listener socket at hostname:port
	 * 
	 */
	public void init(String hostname, int port)
	{
		try 
		{
			this.socket = new Socket(hostname, port);
		} 
		catch (Exception e) 
		{
			LOG.error(e.getMessage(), e);
		}
		
		try
		{
			this.socket.setSoTimeout(SO_TIMEOUT);
		}
		catch (SocketException e)
		{
			//
		}
		
		this.running = true;
	}
	
	public boolean isRunning() 
	{
		return running;
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
			LOG.error("Listener Client connection initialization failed.", e);
			closeConnection();
			ApplicationService.setStatus(ServiceStatus.LISTENER_CLIENT_INIT_FAILED);
			return;
		}
		
		LOG.debug(MessageFormat.format("Running Listener client with session {0}", this.sessionId));
		
		while (this.running)
		{
			try
			{
				Thread.sleep(1);
				
				LOG.trace("Reading messages");
				String message = readMessage();
				
				if(StringUtils.isEmpty(message))
					continue;
				
				LOG.trace(MessageFormat.format("* RECEIVED *** session {0} *** {1}", this.sessionId, message));

				Telegram receivedTelegram = null;
				Telegram responseTelegram = null;
				
				try
				{
					LOG.trace("Deserializing received telegram.");
					receivedTelegram = TelegramSerializer.getInstance().deserialize(message);
					LOG.trace("Processing received telegram.");
					responseTelegram = processReceived(receivedTelegram, message);
					LOG.trace("Received telegram process is completed.");
				}
				catch (Exception e)
				{
					TelegramError telegramError = new TelegramError();
					telegramError.setRejectedTelegram(message);
					responseTelegram = telegramError.toTelegram();
					LOG.error(e.getMessage(), e);
				}
				
				if (responseTelegram == null)
				{
					responseTelegram = Telegram.build(VoidTelegram.class);
				}

				String response = TelegramSerializer.getInstance().fastSerialize(responseTelegram);

				LOG.trace(MessageFormat.format("* RESPONSE *** session {0} *** {1}", this.sessionId, response));

				sendResponse(response);
			}
			catch (Exception e) 
			{
				LOG.error("Client communication interrupted.", e);
				closeConnection();
				ApplicationService.setStatus(ServiceStatus.LISTENER_CLIENT_INIT_FAILED);
				break;
			}	
		}
	}
	
	/**
	 * Processing received messages
	 * 
	 */
	private Telegram processReceived(Telegram receivedTelegram, String receivedXml)
	{
		if(PlayerMessage.class.getSimpleName().equals(receivedTelegram.getTag()))
		{
			PlayerMessage playerMessage = new PlayerMessage();
			try
			{
				playerMessage.loadFromTelegram(receivedTelegram);
			}
			catch (Exception e)
			{
				TelegramError telegramError = new TelegramError();
				telegramError.setRejectedTelegram(receivedXml);
				LOG.error(e.getMessage(), e);
				return telegramError.toTelegram();
			}

			MessageManager.getInstance().receiveMessage(playerMessage);
		}
		
		return null;
	}
	
	/**
	 * Creating Listener reader, writer on socket connection
	 * 
	 */
	private void openConnection() throws IOException
	{
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
		this.writer = new DataOutputStream(this.socket.getOutputStream());
	}

	/**
	 * Sending response message against to received message
	 * 
	 */
	private void sendResponse(String response)
	{
		try
		{
			this.writer.writeBytes(response + "\r\n");

		}
		catch (IOException e)
		{
			//
		}
	}
	
	/**
	 * Reading received message from socket connection
	 * 
	 */
	private String readMessage()
	{
		List<String> lines = new ArrayList<String>();

		try
		{
			while (this.running)
			{
				if(!this.reader.ready())
					continue;
				
				String line = this.reader.readLine();

				if (line == null)
					return null;

				if (lines.isEmpty()
						&& !StringUtils.startsWithIgnoreCase(line, TelegramSerializer.TELEGRAM_ROOT_START_ELEMENT))
				{
					continue;
				}

				lines.add(line);

				if (StringUtils.endsWithIgnoreCase(line, TelegramSerializer.TELEGRAM_ROOT_END_ELEMENT))
				{
					break;
				}
			}

		}
		catch (Exception e)
		{
			LOG.error("Reading message failed.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (String ln : lines)
		{
			sb.append(ln);
		}

		return sb.toString();
	}
	
	/**
	 * Closing connection gracefully
	 * 
	 */
	public void closeConnection()
	{
		LOG.debug(MessageFormat.format("Closing Listener Client session {0}", this.sessionId));
		this.running = false;
		
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
			this.socket.close();
		}
		catch (IOException e)
		{
			// do nothing
		}

		try
		{
			if (sessionId != null)
			{
				SessionManager.unregisterListenerSession(sessionId);
			}
		}
		catch (Exception e)
		{
			// do nothing
		}
		
		LOG.debug(MessageFormat.format("Listener Client session {0} closed", this.sessionId));
	}

}
