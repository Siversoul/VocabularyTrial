package com.visparu.vocabularytrial.model.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.JDBC;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.util.C11N;

public final class ConnectionDetails
{
	
	private static ConnectionDetails instance;
	
	private String	driver;
	private String	protocol;
	private String	filename;
	
	private Connection connection;
	
	static
	{
		try
		{
			DriverManager.registerDriver(new JDBC());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private ConnectionDetails(final String driver, final String protocol, final String filename)
	{
		this.driver		= driver;
		this.protocol	= protocol;
		this.filename	= filename;
		try
		{
			this.connection = DriverManager.getConnection(this.getConnectionString());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static final ConnectionDetails getInstance()
	{
		if (ConnectionDetails.instance == null)
		{
			ConnectionDetails instance = ConnectionDetails.getInstance(
				C11N.getDriver(),
				C11N.getProtocol(),
				C11N.getDatabasePath().getAbsolutePath());
			return instance;
		}
		ConnectionDetails instance = ConnectionDetails.instance;
		return instance;
	}
	
	private static final ConnectionDetails getInstance(final String driver, final String protocol, final String filename)
	{
		ConnectionDetails.instance = new ConnectionDetails(driver, protocol, filename);
		Translation.clearCache();
		Word.clearCache();
		Language.clearCache();
		ConnectionDetails instance = ConnectionDetails.instance;
		return instance;
	}
	
	public final void activateForeignKeyPragma()
	{
		LogItem.enter();
		this.executeSimpleStatement("PRAGMA foreign_keys = ON");
		LogItem.exit();
	}
	
	public final void changeDatabase(final String driver, final String protocol, final String filename)
	{
		ConnectionDetails.getInstance(driver, protocol, filename);
		LogItem.createTable();
		LogItem.enter();
		Language.createTable();
		Word.createTable();
		Translation.createTable();
		Trial.createTable();
		WordCheck.createTable();
		LogItem.debug("All tables created for " + filename);
		LogItem.exit();
	}
	
	public final void copyDatabase(final File newFile)
	{
		LogItem.enter();
		try
		{
			Files.copy(Paths.get(this.filename), new FileOutputStream(newFile));
			LogItem.debug("Database copied to location " + newFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public final void executeSimpleStatement(final String query)
	{
		LogItem.enter();
		final String connString = this.getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final Statement stmt = conn.createStatement())
		{
			stmt.execute(query);
			LogItem.debug("Simple statement executed", query);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		LogItem.exit();
	}
	
	private final String getConnectionString()
	{
		String ret = String.format("%s:%s:%s", this.driver, this.protocol, this.filename);
		return ret;
	}
	
	public final Connection getConnection()
	{
		return this.connection;
	}
	
}
