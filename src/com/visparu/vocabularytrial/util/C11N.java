package com.visparu.vocabularytrial.util;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class C11N
{

	private static final String	DEFAULT_CONFIG_FILE	= "config.json";

	private static final String	DEFAULT_DRIVER		= "jdbc";
	private static final String	DEFAULT_PROTOCOL	= "sqlite";
	private static final String	DEFAULT_FILENAME	= IOUtil.DATA_PATH + "temp.db";

	public static final String getDriver()
	{
		final String driver = C11N.getValue("driver");
		if (driver == null)
		{
			return C11N.DEFAULT_DRIVER;
		}
		return driver;
	}

	public static final void setDriver(String driver)
	{
		C11N.setValue("driver", driver);
	}

	public static final String getProtocol()
	{
		final String protocol = C11N.getValue("protocol");
		if (protocol == null)
		{
			return C11N.DEFAULT_PROTOCOL;
		}
		return protocol;
	}

	public static final void setProtocol(String protocol)
	{
		C11N.setValue("protocol", protocol);
	}

	public static final File getDatabasePath()
	{
		final String dbPath = C11N.getValue("dbPath");
		if (dbPath == null)
		{
			return Paths.get(C11N.DEFAULT_FILENAME).toFile();
		}
		return Paths.get(dbPath).toFile();
	}

	public static final void setDatabasePath(String databasePath)
	{
		C11N.setValue("dbPath", databasePath);
	}

	public static final Locale getLocale()
	{
		final String localeString = C11N.getValue("locale");
		if (localeString == null)
		{
			return I18N.getDefaultLocale();
		}
		return Locale.forLanguageTag(localeString);
	}

	public static final void setLocale(Locale locale)
	{
		C11N.setValue("locale", locale.toLanguageTag());
		I18N.localeProperty().set(locale);
	}

	@SuppressWarnings("unchecked")
	private static final <V> V getValue(String key)
	{
		final String jsonString = IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		if (jsonString == null)
		{
			return null;
		}
		final JSONParser parser = new JSONParser();
		try
		{
			final JSONObject obj = (JSONObject) parser.parse(jsonString);
			final V value = (V) obj.get(key);
			return value;
		}
		catch (ParseException | ClassCastException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static final void setValue(Object key, Object value)
	{
		final String jsonString = IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		final JSONObject obj;
		if(jsonString == null)
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
				return;
			}
		}
		obj.put(key, value);
		IOUtil.writeString(obj.toJSONString(), C11N.DEFAULT_CONFIG_FILE);
	}

}
