package com.visparu.vocabularytrial.util;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public final class C11N
{
	
	private static final String DEFAULT_CONFIG_FILE = "config.json";
	
	private static final String	DEFAULT_DRIVER		= "jdbc";
	private static final String	DEFAULT_PROTOCOL	= "sqlite";
	private static final String	DEFAULT_FILENAME	= IOUtil.DATA_PATH + "temp.db";
	
	public static final String getDriver()
	{
		LogItem.enter();
		final String driver = C11N.getValue("driver");
		if (driver == null)
		{
			LogItem.exit();
			return C11N.DEFAULT_DRIVER;
		}
		LogItem.exit();
		return driver;
	}
	
	public static final void setDriver(String driver)
	{
		LogItem.enter();
		C11N.setValue("driver", driver);
		LogItem.debug("Database driver changed to " + driver);
		LogItem.exit();
	}
	
	public static final String getProtocol()
	{
		LogItem.enter();
		final String protocol = C11N.getValue("protocol");
		if (protocol == null)
		{
			LogItem.exit();
			return C11N.DEFAULT_PROTOCOL;
		}
		LogItem.exit();
		return protocol;
	}
	
	public static final void setProtocol(String protocol)
	{
		LogItem.enter();
		C11N.setValue("protocol", protocol);
		LogItem.debug("Database protocol changed to " + protocol);
		LogItem.exit();
	}
	
	public static final File getDatabasePath()
	{
		LogItem.enter();
		final String dbPath = C11N.getValue("dbPath");
		if (dbPath == null)
		{
			File f = Paths.get(C11N.DEFAULT_FILENAME).toFile();
			LogItem.exit();
			return f;
		}
		File f = Paths.get(dbPath).toFile();
		LogItem.exit();
		return f;
	}
	
	public static final void setDatabasePath(String databasePath)
	{
		LogItem.enter();
		C11N.setValue("dbPath", databasePath);
		LogItem.debug("Database path changed to " + databasePath);
		LogItem.exit();
	}
	
	public static final Locale getLocale()
	{
		LogItem.enter();
		final String localeString = C11N.getValue("locale");
		if (localeString == null)
		{
			Locale l = I18N.getDefaultLocale();
			LogItem.exit();
			return l;
		}
		Locale l = Locale.forLanguageTag(localeString);
		LogItem.exit();
		return l;
	}
	
	public static final void setLocale(Locale locale)
	{
		LogItem.enter();
		C11N.setValue("locale", locale.toLanguageTag());
		I18N.localeProperty().set(locale);
		LogItem.debug("Locale changed to " + locale.getDisplayName());
		LogItem.exit();
	}
	
	@SuppressWarnings("unchecked")
	private static final <V> V getValue(String key)
	{
		LogItem.enter();
		final String jsonString = IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		if (jsonString == null)
		{
			LogItem.exit();
			return null;
		}
		final JSONParser parser = new JSONParser();
		try
		{
			final JSONObject	obj		= (JSONObject) parser.parse(jsonString);
			final V				value	= (V) obj.get(key);
			LogItem.exit();
			return value;
		}
		catch (ParseException | ClassCastException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static final void setValue(Object key, Object value)
	{
		LogItem.enter();
		final String		jsonString	= IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		final JSONObject	obj;
		if (jsonString == null)
		{
			obj = new JSONObject();
		}
		else
		{
			try
			{
				obj = (JSONObject) new JSONParser().parse(jsonString);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
				LogItem.exit();
				return;
			}
		}
		obj.put(key, value);
		IOUtil.writeString(obj.toJSONString(), C11N.DEFAULT_CONFIG_FILE);
		LogItem.exit();
	}
	
}
