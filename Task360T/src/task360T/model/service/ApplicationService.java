package task360T.model.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import task360T.model.service.enumtype.ServiceStatus;
import task360T.model.service.test.TestService;
import task360T.model.service.test.TestService5;
import task360T.model.service.test.TestService7;

/*
 * ApplicationService includes main
 * 
 * */
public class ApplicationService 
{
	private final Logger LOG = Logger.getLogger(ApplicationService.class);
	private static ServiceStatus serviceStatus = ServiceStatus.INITIALIZING;
	
	public ApplicationService()
	{
		super();
	}

	/*
	 * Main function can have two arguments, if they are not provided, it runs with default params.
	 * Argument 0 : Configuration file path. Default is current path.
	 * Argument 1 : Testcase number. Default is 0.
	 * 		0 for Task 5. 
	 * 		1 for Task 7 for initiator. 
	 * 		2 or any other value for Task 7 for receiver.
	 * 
	 * If there is any failure during reading arguments or config file, it runs with default params.
	 * */
	public static void main(String[] args) 
	{
		System.out.println(MessageFormat.format("{0} Started", Constants.APP_NAME));
		
		loadArguments(args);
		
		ApplicationService appService = new ApplicationService();
		appService.init();
		appService.start();
		appService.close();
		
		System.out.println(MessageFormat.format("{0} Finished", Constants.APP_NAME));
	}
	
	public static synchronized void setStatus(ServiceStatus status)
	{
		ApplicationService.serviceStatus = status;
	}
	
	public static synchronized ServiceStatus getStatus()
	{
		return ApplicationService.serviceStatus;
	}
	
	private void init()
	{
		LOG.debug("Initializing Application Service");
		
		RecoveryService.getInstance().init();
		RecoveryService.getInstance().start();
		
		while(ApplicationService.getStatus() != ServiceStatus.STATUS_OK)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
		LOG.debug("Application Service is initialized");
	}
	
	private void start()
	{
		LOG.debug(MessageFormat.format("Application Service is started for test case {0}", AppData.TESTCASE));
		
		TestService testService = null;
		if(AppData.TESTCASE > 0)
		{
			TestService7 testService7 = new TestService7(AppData.TESTCASE == 1);
			testService = testService7;
			testService7.start();
		}
		else
		{
			TestService5 testService5 = new TestService5();
			testService = testService5;
			testService5.start();
		}
		
		while (!testService.isCompleted())
		{
			try
			{
				// Keeping main thread alive.
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				LOG.warn("Service interrupted.", e);
			}
		}
		
		LOG.debug(MessageFormat.format("Application Service is completed for test case {0}", AppData.TESTCASE));
	}
	
	private void close()
	{
		LOG.debug("Application Service sessions will be closed");
		
		RecoveryService.getInstance().close();
		
		LOG.debug("Application Service sessions are closed");
	}
	
	private static void loadArguments(String[] args)
	{
		if(args != null && args.length > 0)
		{
			System.out.print("Arguments: ");
			for(String arg : args)
			{
				System.out.print(arg + " ");
			}
			System.out.println("");
			
			try
			{
				AppData.CFG_PATH = args[0];
				
				if(!AppData.CFG_PATH.endsWith(File.separator))
					AppData.CFG_PATH=AppData.CFG_PATH.concat(File.separator);
				
				if(args.length > 1)
				{
					AppData.TESTCASE = Integer.valueOf(args[1]);
				}
			}
			catch (Exception e) 
			{
				System.err.println("Failed to parse arguments");
			}
		}

		loadLog4j();
		loadAppProperties();
	}
	
	private static void loadLog4j()
	{
		try {
			PropertyConfigurator.configure(MessageFormat.format("{0}{1}", AppData.CFG_PATH, Constants.LOG4J_FILE));
			
			System.out.println(MessageFormat.format("Log4j properties is loaded from {0}{1}",
					AppData.CFG_PATH, Constants.LOG4J_FILE));
		} 
		catch (Exception e) 
		{
			System.err.println(MessageFormat.format("Cannot initialize log4j from {0}{1} :{2}",
					AppData.CFG_PATH, Constants.LOG4J_FILE, e.getMessage()));
		}
	}
	
	private static void loadAppProperties()
	{
		Properties props;
		try {
			props = loadPropertiesFile(MessageFormat.format("{0}{1}", AppData.CFG_PATH,
					Constants.TASK_360T_CFG));
			
			AppData.TCP_SERVER_SUBSCRIPTION_PORT_NUMBER = Integer.valueOf(props
					.getProperty(Constants.SUBSCRIPTION_PORT_NUMBER));
			AppData.TCP_CLIENT_LISTEN_PORT_NUMBER = Integer.valueOf(props
					.getProperty(Constants.LISTEN_PORT_NUMBER));
			AppData.TCP_CLIENT_LISTEN_HOST_NAME = props
					.getProperty(Constants.LISTEN_HOST_NAME);
			

			System.out.println(MessageFormat.format("Configuration parameters are loaded from file {0}{1}.", 
					AppData.CFG_PATH, Constants.TASK_360T_CFG));
			
		} catch (IOException e) {
			System.out.println(MessageFormat.format("Failed to read configuration file {0}{1}. Default testcase will be run.", 
					AppData.CFG_PATH, Constants.TASK_360T_CFG));
			
			AppData.TESTCASE = 0;
		}
	}
	
	private static Properties loadPropertiesFile(String path) throws IOException
	{
		InputStream inputStream = null;

		try
		{
			Properties props = new Properties();

			File file = new File(path);
			inputStream = FileUtils.openInputStream(file);

			props.load(inputStream);
			return props;
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}
}
