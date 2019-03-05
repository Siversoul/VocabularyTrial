package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;

public final class Trial
{
	
	private static final Map<Integer, Trial> cache = new HashMap<>();
	
	private Integer			trial_id;
	private final Date		date;
	private final Language	language_from;
	private final Language	language_to;
	
	public Trial(final Integer trial_id, final Date date, final Language language_from, final Language language_to)
	{
		LogItem.enter();
		this.trial_id		= trial_id;
		this.date			= date;
		this.language_from	= language_from;
		this.language_to	= language_to;
		LogItem.debug("Initialized new trial " + Trial.getDateFormatter().format(date) + "");
		LogItem.exit();
	}
	
	public static final void createTable()
	{
		LogItem.enter();
		ConnectionDetails.getInstance()
			.executeSimpleStatement("CREATE TABLE IF NOT EXISTS trial(" + "trial_id INTEGER PRIMARY KEY AUTOINCREMENT," + "datetime VARCHAR(23), " + "language_code_from VARCHAR(2), "
				+ "language_code_to VARCHAR(2), " + "FOREIGN KEY(language_code_from) REFERENCES language(language_code) ON UPDATE CASCADE ON DELETE CASCADE, "
				+ "FOREIGN KEY(language_code_to) REFERENCES language(language_code) ON UPDATE CASCADE ON DELETE CASCADE)");
		LogItem.debug("Trial table created");
		LogItem.exit();
	}
	
	public static final void clearCache()
	{
		LogItem.enter();
		Trial.cache.clear();
		LogItem.debug("Cleared trial cache");
		LogItem.exit();
	}
	
	public static final Trial get(final Integer trial_id)
	{
		LogItem.enter();
		if (Trial.cache.containsKey(trial_id))
		{
			Trial t = Trial.cache.get(trial_id);
			LogItem.exit();
			return t;
		}
		Trial t = Trial.readEntity(trial_id);
		LogItem.exit();
		return t;
	}
	
	public static final Trial createTrial(final Date date, final Language language_from, final Language language_to)
	{
		LogItem.enter();
		final Trial		t			= new Trial(-1, date, language_from, language_to);
		final Integer	trial_id	= Trial.writeEntity(t);
		t.setTrial_id(trial_id);
		Trial.cache.put(trial_id, t);
		TrialComponent.repopulateAllTrials();
		LogItem.exit();
		return t;
	}
	
	public static final void removeTrial(final Integer trial_id)
	{
		LogItem.enter();
		String date = Trial.getDateFormatter().format(Trial.get(trial_id));
		Trial.cache.remove(trial_id);
		final String		query	= "DELETE FROM trial " + "WHERE trial_id = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, trial_id);
			pstmt.execute();
			LogItem.debug("Trial at " + date + " removed");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public static final void removeAllTrials()
	{
		LogItem.enter();
		Trial.clearCache();
		ConnectionDetails.getInstance().executeSimpleStatement("DELETE FROM trial");
		LogItem.debug("All trials removed");
		LogItem.exit();
	}
	
	private static final Trial readEntity(final Integer trial_id)
	{
		LogItem.enter();
		final String		query	= "SELECT * " + "FROM trial " + "WHERE trial_id = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, trial_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final String	dateString		= rs.getString("datetime");
				final Date		date			= Trial.getDateFormatter().parse(dateString);
				final String	l_fromString	= rs.getString("language_code_from");
				final Language	l_from			= Language.get(l_fromString);
				final String	l_toString		= rs.getString("language_code_to");
				final Language	l_to			= Language.get(l_toString);
				final Trial		t				= new Trial(trial_id, date, l_from, l_to);
				Trial.cache.put(trial_id, t);
				rs.close();
				LogItem.exit();
				return t;
			}
			rs.close();
		}
		catch (SQLException | ParseException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return null;
	}
	
	private static final Integer writeEntity(final Trial trial)
	{
		LogItem.enter();
		final String		query	= "INSERT INTO trial(datetime, language_code_from, language_code_to) " + "VALUES(?, ?, ?)";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			final String dateString = Trial.getDateFormatter().format(trial.getDate());
			pstmt.setString(1, dateString);
			pstmt.setString(2, trial.getLanguage_from().getLanguage_code());
			pstmt.setString(3, trial.getLanguage_to().getLanguage_code());
			pstmt.executeUpdate();
			final ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			Integer trial_id = rs.getInt(1);
			LogItem.debug("Inserted new trial entity at " + dateString);
			LogItem.exit();
			return trial_id;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return -1;
	}
	
	public static final List<Trial> getTrials(final Language l_from, final Language l_to)
	{
		LogItem.enter();
		final List<Trial>	trials	= new ArrayList<>();
		final String		query	= "SELECT trial_id " + "FROM trial " + "WHERE language_code_from = ? " + "AND language_code_to = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, l_from.getLanguage_code());
			pstmt.setString(2, l_to.getLanguage_code());
			final ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				final int	trial_id	= rs.getInt("trial_id");
				final Trial	t			= Trial.get(trial_id);
				trials.add(t);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return trials;
	}
	
	public static final SimpleDateFormat getDateFormatter()
	{
		LogItem.enter();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		LogItem.exit();
		return sdf;
	}
	
	public final Integer getTrial_id()
	{
		LogItem.enter();
		LogItem.exit();
		return this.trial_id;
	}
	
	private final void setTrial_id(final Integer trial_id)
	{
		LogItem.enter();
		this.trial_id = trial_id;
		LogItem.exit();
	}
	
	public final Date getDate()
	{
		LogItem.enter();
		LogItem.exit();
		return this.date;
	}
	
	public final Language getLanguage_from()
	{
		LogItem.enter();
		LogItem.exit();
		return this.language_from;
	}
	
	public Language getLanguage_to()
	{
		LogItem.enter();
		LogItem.exit();
		return this.language_to;
	}
	
	public final List<WordCheck> getWordChecks()
	{
		LogItem.enter();
		final List<WordCheck>	wordchecks	= new ArrayList<>();
		final String			query		= "SELECT * " + "FROM wordcheck " + "WHERE trial_id = ?";
		final Connection		conn		= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, this.trial_id);
			final ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				final Integer	word_id	= rs.getInt("word_id");
				final WordCheck	wc		= WordCheck.get(Word.get(word_id), this);
				wordchecks.add(wc);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return wordchecks;
	}
	
}
