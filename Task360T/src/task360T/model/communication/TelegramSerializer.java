package task360T.model.communication;


import java.io.StringReader;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import task360T.model.communication.entity.Telegram;

/**
 * Prepares the telegram messages during communication
 * 
 * */
public class TelegramSerializer
{
	private static final Logger LOG = Logger.getLogger(TelegramSerializer.class);

	public static final String TELEGRAM_TAG = "telegram";
	public static final String TELEGRAM_ROOT_START_ELEMENT = "<telegram>";
	public static final String TELEGRAM_ROOT_END_ELEMENT = "</telegram>";
	
	private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder docBuilder;

	public static TelegramSerializer instance;

	private TelegramSerializer() throws ParserConfigurationException
	{
		this.docBuilder = docFactory.newDocumentBuilder();
	}

	/**
	 * Converts the telegram message to xml 
	 * 
	 * */
	public String fastSerialize(Telegram telegram)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(TELEGRAM_ROOT_START_ELEMENT);
		
		for (Entry<String, String> field : telegram.getFields())
		{
			String fieldName = field.getKey();
			String fieldValue = field.getValue();

			if (fieldValue == null)
				continue;
			
			fieldName = StringEscapeUtils.escapeXml11(fieldName);
			fieldValue = StringEscapeUtils.escapeXml11(fieldValue);

			appendMessage(sb, fieldName, fieldValue);
		}

		sb.append(TELEGRAM_ROOT_END_ELEMENT);

		return sb.toString();
	}
	
	private static void appendMessage(StringBuilder sb, String fieldName, String fieldValue)
	{
		sb.append("<");
		sb.append(fieldName);
		sb.append(">");

		sb.append(fieldValue);

		sb.append("</");
		sb.append(fieldName);
		sb.append(">");
	}
	
	/**
	 * Converts received message xml to telegram message
	 * 
	 * */
	public Telegram deserialize(String xml) throws Exception
	{
		InputSource is = new InputSource(new StringReader(xml));
		Document doc = docBuilder.parse(is);

		Element rootElement = doc.getDocumentElement();

		NodeList nodeList = rootElement.getChildNodes();

		Telegram telegram = new Telegram();

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			String fieldName = node.getNodeName();
			String value = node.getTextContent();

			telegram.set(fieldName, value);
		}

		return telegram;
	}

	public static TelegramSerializer getInstance()
	{
		if (instance == null)
		{
			try
			{
				instance = new TelegramSerializer();
			}
			catch (ParserConfigurationException e)
			{
				LOG.fatal("TelegramSerializer initialization failed.", e);
			}
		}
		return instance;
	}

}

