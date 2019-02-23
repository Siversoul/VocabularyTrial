package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.visparu.vocabularytrial.gui.interfaces.LogComponent;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;
import com.visparu.vocabularytrial.model.log.Severity;

public final class LogItem
{
	
	private static final Map<Integer, LogItem> cache = new HashMap<>();
	
	private static Integer session_log_id = -1;
	
	private Integer			logitem_id;
	private Integer			log_id;
	private Severity		severity;
	private LocalDateTime	datetime;
	private String			threadName;
	private String			function;
	private String			message;
	private String			description;
	
	private LogItem(final Integer logitem_id, final Integer log_id, final Severity severity, final LocalDateTime datetime, final String threadName, final String function, final String message,
		final String description)
	{
		this.logitem_id		= logitem_id;
		this.log_id			= log_id;
		this.severity		= severity;
		this.datetime		= datetime;
		this.threadName		= threadName;
		this.function		= function;
		this.message		= message;
		this.description	= description;
	}
	
	public final static void createTable()
	{
		ConnectionDetails.getInstance().executeSimpleStatement("CREATE TABLE IF NOT EXISTS logitem ("
			+ "logitem_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "log_id INTEGER, "
			+ "severity VARCHAR(20), "
			+ "datetime VARCHAR(23), "
			+ "threadName VARCHAR(100), "
			+ "function VARCHAR(100), "
			+ "message VARCHAR(200), "
			+ "description VARCHAR(500)");
	}
	
	public final static void initializeNewLogSession()
	{
		final String query = "SELECT max(log_id) FROM logitem";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try(Connection conn = DriverManager.getConnection(connString); Statement stmt = conn.createStatement())
		{
			final ResultSet rs = stmt.executeQuery(query);
			final Integer next_log_id;
			if(rs.next())
			{
				final Integer max_log_id = rs.getInt(1);
				next_log_id = max_log_id + 1;
			}
			else
			{
				next_log_id = 1;
			}
			rs.close();
			LogItem.session_log_id = next_log_id;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public final static void clearCache()
	{
		LogItem.cache.clear();
	}
	
	public final static LogItem get(final Integer logitem_id)
	{
		if (LogItem.cache.containsKey(logitem_id))
		{
			return LogItem.cache.get(logitem_id);
		}
		return LogItem.readEntity(logitem_id);
	}
	
	public final static LogItem enter()
	{
		return LogItem.trace("Entered method (Depth: " + Thread.currentThread().getStackTrace().length + ")");
	}
	
	public final static LogItem exit()
	{
		return LogItem.trace("Exited method (Depth: " + Thread.currentThread().getStackTrace().length + ")");
	}
	
	public final static LogItem trace(String message)
	{
		return LogItem.createLogItem(Severity.TRACE, message);
	}
	
	public final static LogItem trace(String message, String description)
	{
		return LogItem.createLogItem(Severity.TRACE, message, description);
	}
	
	public final static LogItem debug(String message)
	{
		return LogItem.createLogItem(Severity.DEBUG, message);
	}
	
	public final static LogItem debug(String message, String description)
	{
		return LogItem.createLogItem(Severity.DEBUG, message, description);
	}
	
	public final static LogItem info(String message)
	{
		return LogItem.createLogItem(Severity.INFO, message);
	}
	
	public final static LogItem info(String message, String description)
	{
		return LogItem.createLogItem(Severity.INFO, message, description);
	}
	
	public final static LogItem warning(String message)
	{
		return LogItem.createLogItem(Severity.WARNING, message);
	}
	
	public final static LogItem warning(String message, String description)
	{
		return LogItem.createLogItem(Severity.WARNING, message, description);
	}
	
	public final static LogItem error(String message)
	{
		return LogItem.createLogItem(Severity.ERROR, message);
	}
	
	public final static LogItem error(String message, String description)
	{
		return LogItem.createLogItem(Severity.ERROR, message, description);
	}
	
	public final static LogItem critical(String message)
	{
		return LogItem.createLogItem(Severity.CRITICAL, message);
	}
	
	public final static LogItem critical(String message, String description)
	{
		return LogItem.createLogItem(Severity.CRITICAL, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final String message)
	{
		String description = new String(message);
		return LogItem.createLogItem(severity, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final String message, final String description)
	{
		LocalDateTime		datetime	= LocalDateTime.now();
		String				threadname	= Thread.currentThread().getName();
		StackTraceElement[]	stackTrace	= Thread.currentThread().getStackTrace();
		String				function	= "n/A";
		if (stackTrace.length >= 3)
		{
			function = String.format("%s.%s:%d", stackTrace[2].getClassName(), stackTrace[2].getMethodName(), stackTrace[2].getLineNumber());
		}
		return LogItem.createLogItem(severity, datetime, threadname, function, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final LocalDateTime datetime, final String threadname, final String function, final String message, final String description)
	{
		LogItem			li			= new LogItem(-1, LogItem.session_log_id, severity, datetime, threadname, function, message, description);
		final Integer	logitem_id	= LogItem.writeEntity(li);
		li.setLogitem_id(logitem_id);
		LogItem.cache.put(logitem_id, li);
		LogComponent.repopulateAllLogs();
		return li;
	}
	
	public final static void removeLogItem(final Integer logitem_id)
	{
		LogItem.cache.remove(logitem_id);
		final String	query		= "DELETE FROM logitem WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, logitem_id);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogComponent.repopulateAllLogs();
	}
	
	public final static void removeAllLogItems()
	{
		LogItem.clearCache();
		ConnectionDetails.getInstance().executeSimpleStatement("DELETE FROM logitem");
		LogComponent.repopulateAllLogs();
	}
	
	private final static LogItem readEntity(final Integer logitem_id)
	{
		final String	query		= "SELECT * FROM logitem WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, logitem_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final Integer		log_id		= rs.getInt("log_id");
				final Severity		severity	= Severity.valueOf(rs.getString("severity"));
				final LocalDateTime	datetime	= LocalDateTime.parse(rs.getString("datetime"));
				final String		threadName	= rs.getString("threadname");
				final String		function	= rs.getString("function");
				final String		message		= rs.getString("message");
				final String		description	= rs.getString("description");
				final LogItem		li			= new LogItem(logitem_id, log_id, severity, datetime, threadName, function, message, description);
				LogItem.cache.put(logitem_id, li);
				rs.close();
				return li;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private final static Integer writeEntity(final LogItem logitem)
	{
		final String	query		= "INSERT INTO logitem(severity, datetime, threadname, function, message, description) VALUES(?, ?, ?, ?, ?, ?)";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, logitem.getSeverity().toString());
			pstmt.setString(2, logitem.getDatetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			pstmt.setString(3, logitem.getThreadName());
			pstmt.setString(4, logitem.getFunction());
			pstmt.setString(5, logitem.getMessage());
			pstmt.setString(6, logitem.getDescription());
			pstmt.executeUpdate();
			final ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			final Integer logitem_id = rs.getInt(1);
			rs.close();
			return logitem_id;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public final Integer getLogitem_id()
	{
		return this.logitem_id;
	}
	
	private final void setLogitem_id(final Integer logitem_id)
	{
		this.logitem_id = logitem_id;
	}
	
	public Integer getLog_id()
	{
		return this.log_id;
	}
	
	public Severity getSeverity()
	{
		return this.severity;
	}
	
	public void setSeverity(Severity severity)
	{
		final String	query		= "UPDATE logitem "
			+ "SET severity = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, severity.toString());
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.severity = severity;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public LocalDateTime getDatetime()
	{
		return this.datetime;
	}
	
	public void setDatetime(LocalDateTime datetime)
	{
		final String	query		= "UPDATE logitem "
			+ "SET datetime = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, datetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.datetime = datetime;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getThreadName()
	{
		return this.threadName;
	}
	
	public void setThreadName(String threadName)
	{
		final String	query		= "UPDATE logitem "
			+ "SET threadname = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, threadName);
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.threadName = threadName;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getFunction()
	{
		return function;
	}
	
	public void setFunction(String function)
	{
		final String	query		= "UPDATE logitem "
			+ "SET function = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, function);
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.function = function;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public void setMessage(String message)
	{
		final String	query		= "UPDATE logitem "
			+ "SET message = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, message);
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.message = message;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public void setDescription(String description)
	{
		final String	query		= "UPDATE logitem "
			+ "SET description = ? "
			+ "WHERE logitem_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, description);
			pstmt.setInt(2, this.logitem_id);
			pstmt.executeUpdate();
			this.description = description;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
