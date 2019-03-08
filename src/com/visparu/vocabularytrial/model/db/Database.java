package com.visparu.vocabularytrial.model.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sqlite.JDBC;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.util.C11N;

public final class Database
{
	private static Database	instance;
	private String			driver;
	private String			protocol;
	private String			filename;
	private Connection		connection;
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
	
	private Database(final String driver, final String protocol, final String filename)
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
	
	public static final Database get()
	{
		if (Database.instance == null)
		{
			Database instance = Database.get(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			return instance;
		}
		Database instance = Database.instance;
		return instance;
	}
	
	private static final Database get(final String driver, final String protocol, final String filename)
	{
		Database.instance = new Database(driver, protocol, filename);
		Translation.clearCache();
		Word.clearCache();
		Language.clearCache();
		Database instance = Database.instance;
		return instance;
	}
	
	public final void activateForeignKeyPragma()
	{
		try (final PreparedStatement pstmt = this.prepareStatement("PRAGMA foreign_keys = ON"))
		{
			pstmt.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public final void changeDatabase(final String driver, final String protocol, final String filename)
	{
		Database.get(driver, protocol, filename);
		LogItem.createTable();
		Language.createTable();
		Word.createTable();
		Translation.createTable();
		Trial.createTable();
		WordCheck.createTable();
		LogItem.debug("All tables created for " + filename);
	}
	
	public final void copyDatabase(final File newFile)
	{
		try
		{
			Files.copy(Paths.get(this.filename), new FileOutputStream(newFile));
			LogItem.debug("Database copied to location " + newFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public final PreparedStatement prepareStatement(final String query) throws SQLException
	{
		final Connection		conn	= this.getConnection();
		final PreparedStatement	pstmt	= conn.prepareStatement(query);
		return pstmt;
	}
	
	private final void fillPreparedStatement(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		for (int i = 0; i < params.length; i++)
		{
			final Object param = params[i];
			if (param instanceof Integer)
			{
				final Integer param_i = (Integer) param;
				pstmt.setInt(i + 1, param_i);
			}
			else if (param instanceof String)
			{
				final String param_s = (String) param;
				pstmt.setString(i + 1, param_s);
			}
			else
			{
				throw new IllegalArgumentException("Type " + param.getClass().getName() + " is not supported!");
			}
		}
	}
	
	public final void execute(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		this.fillPreparedStatement(pstmt, params);
		pstmt.execute();
	}
	
	public final ResultSet executeQuery(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		this.fillPreparedStatement(pstmt, params);
		final ResultSet rs = pstmt.executeQuery();
		return rs;
	}
	
	private final String getConnectionString()
	{
		String ret = String.format("%s:%s:%s", this.driver, this.protocol, this.filename);
		return ret;
	}
	
	private final Connection getConnection()
	{
		return this.connection;
	}
}
