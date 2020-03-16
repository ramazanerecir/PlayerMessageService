package task360T.model.service;

/*
 * Application Configuration parameters
 * 
 * */
public class AppData 
{
	//Socket Server Port to send messages
	public static int TCP_SERVER_SUBSCRIPTION_PORT_NUMBER = 9999;
		
	//Socket Listener Client Port to receive messages
	public static int TCP_CLIENT_LISTEN_PORT_NUMBER = 9999;
	
	//Socket Listener Client Host Name
	public static String TCP_CLIENT_LISTEN_HOST_NAME = "localhost";
	
	//Configuration File Path
	public static String CFG_PATH="./";
	
	//Application Test Case
	//1: Task 5
	//2: Task 7 with initiator role
	//3: Task 7 with receiver role
	public static int TESTCASE=0;	
}
