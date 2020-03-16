package task360T.model.service.enumtype;

/*
 * Application Service status
 * 
 * */
public enum ServiceStatus 
{
	INITIALIZING("Initializing"),
	SUBSCRIPTION_SERVER_INIT_FAILED("Subscription Server Init Failed"),
	LISTENER_CLIENT_INIT_FAILED("Listener Client Init Failed"),
	STATUS_OK("Service is Ready"),
	CLOSING("Closing Service"),
	CLOSED("Service is Closed");
	
	private String message;
	
	private ServiceStatus(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}

}
