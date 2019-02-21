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
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.util.C11N;

public final class ConnectionDetails
{

	private static ConnectionDetails	instance;

	private String						driver;
	private String						protocol;
	private String						filename;

	static
	{
		try
		{
			DriverManager.deregisterDriver(new JDBC());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private ConnectionDetails(final String driver, final String protocol, final String filename)
	{
		this.driver = driver;
		this.protocol = protocol;
		this.filename = filename;
	}

	public static final ConnectionDetails getInstance()
	{
		if (ConnectionDetails.instance == null)
		{
			return ConnectionDetails.getInstance(
					C11N.getDriver(),
					C11N.getProtocol(),
					C11N.getDatabasePath().getAbsolutePath());
		}
		return ConnectionDetails.instance;
	}

	private static final ConnectionDetails getInstance(final String driver, final String protocol, final String filename)
	{
		ConnectionDetails.instance = new ConnectionDetails(driver, protocol, filename);
		Translation.clearCache();
		Word.clearCache();
		Language.clearCache();
		return ConnectionDetails.instance;
	}

	public final void activateForeignKeyPragma()
	{
		this.executeSimpleStatement("PRAGMA foreign_keys = ON");
	}

	public final void changeDatabase(final String driver, final String protocol, final String filename)
	{
		ConnectionDetails.getInstance(driver, protocol, filename);
		Language.createTable();
		Word.createTable();
		Translation.createTable();
		Trial.createTable();
		WordCheck.createTable();
	}

	public final void copyDatabase(final File newFile)
	{
		try
		{
			Files.copy(Paths.get(this.filename), new FileOutputStream(newFile));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public final void executeSimpleStatement(final String query)
	{
		final String connString = this.getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final Statement stmt = conn.createStatement())
		{
			stmt.execute(query);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public final String getConnectionString()
	{
		return String.format("%s:%s:%s", this.driver, this.protocol, this.filename);
	}

}
