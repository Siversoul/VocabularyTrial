package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.visparu.vocabularytrial.gui.interfaces.LogComponent;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;
import com.visparu.vocabularytrial.model.log.Severity;

public final class LogItem
{
	
	private static final Map<Integer, LogItem> cache = new HashMap<>();
	
	private static Integer			session_log_id				= -1;
	private static boolean			initialized					= false;
	private static List<LogItem>	preinitialization_logitems	= new ArrayList<>();
	
	private Integer			logitem_id;
	private Integer			log_id;
	private Severity		severity;
	private LocalDateTime	datetime;
	private String			threadName;
	private String			function;
	private String			message;
	private String			description;
	
	private LogItem(final Integer logitem_id, final Integer log_id, final Severity severity, final LocalDateTime datetime, final String threadName,
		final String function, final String message, final String description)
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
		ConnectionDetails.getInstance()
			.executeSimpleStatement("CREATE TABLE IF NOT EXISTS logitem (" + "logitem_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "log_id INTEGER, "
				+ "severity VARCHAR(20), " + "datetime VARCHAR(23), " + "threadname VARCHAR(100), " + "function VARCHAR(100), "
				+ "message VARCHAR(200), " + "description VARCHAR(500))");
	}
	
	public final static void initializeNewLogSession()
	{
		final String	query		= "SELECT max(log_id) FROM logitem";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (Statement stmt = conn.createStatement())
		{
			final ResultSet	rs	= stmt.executeQuery(query);
			final Integer	next_log_id;
			if (rs.next())
			{
				final Integer max_log_id = rs.getInt(1);
				if (max_log_id < 1)
				{
					next_log_id = 1;
				}
				else
				{
					next_log_id = max_log_id + 1;
				}
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
		LogItem.initialized = true;
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
		final String description = new String(message);
		return LogItem.createLogItem(severity, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final String message, final String description)
	{
		final LocalDateTime			datetime	= LocalDateTime.now();
		final String				threadname	= Thread.currentThread().getName();
		final StackTraceElement[]	stackTrace	= Thread.currentThread().getStackTrace();
		String						function	= "n/A";
		for (int i = 1; i < stackTrace.length; i++)
		{
			final StackTraceElement ste = stackTrace[i];
			if (!ste.getClassName().contentEquals(LogItem.class.getName()))
			{
				function = String.format("%s.%s:%d", ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
				break;
			}
		}
		return LogItem.createLogItem(severity, datetime, threadname, function, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final LocalDateTime datetime, final String threadname, final String function,
		final String message, final String description)
	{
		final LogItem li = new LogItem(-1, LogItem.session_log_id, severity, datetime, threadname, function, message, description);
		LogItem.preinitialization_logitems.add(li);
		if (LogItem.initialized)
		{
			while (!LogItem.preinitialization_logitems.isEmpty())
			{
				if (LogItem.preinitialization_logitems.get(0).getLog_id() == -1)
				{
					LogItem lit = LogItem.preinitialization_logitems.get(0);
					lit.log_id = LogItem.session_log_id;
				}
				final LogItem	lip			= LogItem.preinitialization_logitems.remove(0);
				final Integer	logitem_id	= LogItem.writeEntity(lip);
				li.setLogitem_id(logitem_id);
				LogItem.cache.put(logitem_id, lip);
				LogComponent.repopulateAllLogs();
			}
		}
		return li;
	}
	
	public final static void removeLogItem(final Integer logitem_id)
	{
		LogItem.cache.remove(logitem_id);
		final String	query		= "DELETE FROM logitem WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, logitem_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final Integer		log_id		= rs.getInt("log_id");
				final Severity		severity	= Severity.values()[rs.getInt("severity")];
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
		final String	query		= "INSERT INTO logitem(log_id, severity, datetime, threadname, function, message, description) VALUES(?, ?, ?, ?, ?, ?, ?)";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, logitem.getLog_id());
			pstmt.setInt(2, logitem.getSeverity().ordinal());
			pstmt.setString(3, logitem.getDatetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			pstmt.setString(4, logitem.getThreadName());
			pstmt.setString(5, logitem.getFunction());
			pstmt.setString(6, logitem.getMessage());
			pstmt.setString(7, logitem.getDescription());
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
	
	public static final Integer getSessionLog_id()
	{
		return LogItem.session_log_id;
	}
	
	public final Integer getLogitem_id()
	{
		return this.logitem_id;
	}
	
	private final void setLogitem_id(final Integer logitem_id)
	{
		this.logitem_id = logitem_id;
	}
	
	public final Integer getLog_id()
	{
		return this.log_id;
	}
	
	public final Severity getSeverity()
	{
		return this.severity;
	}
	
	public final void setSeverity(Severity severity)
	{
		final String	query		= "UPDATE logitem " + "SET severity = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public final LocalDateTime getDatetime()
	{
		return this.datetime;
	}
	
	public final void setDatetime(LocalDateTime datetime)
	{
		final String	query		= "UPDATE logitem " + "SET datetime = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public final String getThreadName()
	{
		return this.threadName;
	}
	
	public final void setThreadName(String threadName)
	{
		final String	query		= "UPDATE logitem " + "SET threadname = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public final String getFunction()
	{
		return function;
	}
	
	public final void setFunction(String function)
	{
		final String	query		= "UPDATE logitem " + "SET function = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public final String getMessage()
	{
		return this.message;
	}
	
	public final void setMessage(String message)
	{
		final String	query		= "UPDATE logitem " + "SET message = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public final String getDescription()
	{
		return this.description;
	}
	
	public final void setDescription(String description)
	{
		final String	query		= "UPDATE logitem " + "SET description = ? " + "WHERE logitem_id = ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
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
	
	public static final List<Integer> getAllLogIds()
	{
		final List<Integer>	log_ids		= new ArrayList<>();
		final String		query		= "SELECT DISTINCT log_id FROM logitem";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (final Statement stmt = conn.createStatement())
		{
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
			{
				log_ids.add(rs.getInt("log_id"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return log_ids;
	}
	
	public static final List<LogItem> getAllLogItemsForLog(Integer log_id)
	{
		List<LogItem>	logitems	= new ArrayList<>();
		String			query		= "SELECT logitem_id FROM logitem WHERE log_id = ? AND severity >= ?";
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, log_id);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				LogItem li = LogItem.get(rs.getInt("logitem_id"));
				logitems.add(li);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return logitems;
	}
	
	public static final List<LogItem> getFilteredLogItems(Integer log_id, Severity min_severity, String thread, String function, String message,
		boolean description)
	{
		List<LogItem> logitems = new ArrayList<>();
		
		StringJoiner sj_filter = new StringJoiner(" AND ");
		if (log_id != null)
		{
			sj_filter.add("log_id = ?");
		}
		if (min_severity != null)
		{
			sj_filter.add("severity >= ?");
		}
		if (thread != null)
		{
			sj_filter.add("threadname = ?");
		}
		if (function != null)
		{
			sj_filter.add("function = ?");
		}
		if (message != null)
		{
			if (description)
			{
				sj_filter.add("(message LIKE ? OR description LIKE ?)");
			}
			else
			{
				sj_filter.add("message LIKE ?");
			}
		}
		
		String	query		= "SELECT logitem_id FROM logitem WHERE " + sj_filter.toString();
		final Connection conn = ConnectionDetails.getInstance().getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(query))
		{
			int index = 1;
			if (log_id != null)
			{
				pstmt.setInt(index++, log_id);
			}
			if (min_severity != null)
			{
				pstmt.setInt(index++, min_severity.ordinal());
			}
			if (thread != null)
			{
				pstmt.setString(index++, thread);
			}
			if (function != null)
			{
				pstmt.setString(index++, function);
			}
			if (message != null)
			{
				pstmt.setString(index++, "%" + message + "%");
				if (description)
				{
					pstmt.setString(index++, "%" + message + "%");
				}
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				LogItem li = LogItem.get(rs.getInt("logitem_id"));
				logitems.add(li);
			}
		}
		catch (SQLException e)
		{
			System.err.println(query);
			e.printStackTrace();
		}
		
		return logitems;
	}
	
}
