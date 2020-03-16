package task360T.model.communication.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * Telegram is the basic type which is sent/received as a message during communication
 * 
 * */
public class Telegram implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4525925640265759860L;
	
	public static final String NA = "";

	public static final String TAG = "_TAG";
	public static final String ID = "_ID";
	
	private Map<String, String> fields = new HashMap<String, String>();

	public Telegram()
	{
		super();
	}

	public String getId()
	{
		return fields.get(ID);
	}

	public void setId(String id)
	{
		this.set(ID, id);
	}

	public void setTag(String tag)
	{
		this.set(TAG, tag);
	}

	public String getTag()
	{
		return this.get(TAG);
	}

	public void set(String key, String value)
	{
		this.fields.put(key, value);
	}

	public String get(String key)
	{
		return this.fields.get(key);
	}

	public void set(String key, Long value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, String.valueOf(value));
	}

	public Long getLong(String key)
	{
		String value = this.fields.get(key);

		try
		{
			return Long.valueOf(value);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void set(String key, Integer value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, String.valueOf(value));
	}

	public Integer getInteger(String key)
	{
		String value = this.fields.get(key);

		try
		{
			return Integer.valueOf(value);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void set(String key, Date value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, MessageFormat.format("{0,date,dd.MM.yyyy}", value));
	}

	public void setTime(String key, Date value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, MessageFormat.format("{0,date,HH:mm:ss}", value));
	}

	public Date getDate(String key)
	{
		String value = this.fields.get(key);
		if (value == null)
			return null;

		try
		{
			return new SimpleDateFormat("dd.MM.yyyy").parse(value);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void set(String key, BigDecimal value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		String valString = value.stripTrailingZeros().toPlainString();

		set(key, valString);
	}

	public void set(String key, BigDecimal value, int scale)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, value.setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString());
	}

	public BigDecimal getBigDecimal(String key)
	{
		String value = this.fields.get(key);

		try
		{
			return new BigDecimal(value).stripTrailingZeros();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void set(String key, Boolean value)
	{
		if (value == null)
		{
			set(key, NA);
			return;
		}

		set(key, String.valueOf(value));
	}

	public Boolean getBoolean(String key)
	{
		String value = this.fields.get(key);

		try
		{
			return Boolean.valueOf(value);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public Set<Entry<String, String>> getFields()
	{
		return fields.entrySet();
	}


	public static Telegram build(Class<?> cls)
	{
		Telegram telegram = new Telegram();
		telegram.setTag(cls.getSimpleName());
		telegram.setId(UUID.randomUUID().toString());

		return telegram;
	}

	@Override
	public String toString()
	{
		if (this.fields == null)
			return "BLANK";

		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : this.fields.entrySet())
		{
			sb.append(MessageFormat.format("{0}: {1}", entry.getKey(), entry.getValue()));
		}
		return sb.toString();
	}
}
