package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;

public final class WordCheck
{
	private static final Map<Integer, WordCheck>	cache	= new HashMap<>();
	private final Word								word;
	private final Trial								trial;
	private final String							answerString;
	private final Boolean							correct;
	
	private WordCheck(final Word word, final Trial trial, final String answerString, final Boolean correct)
	{
		this.word			= word;
		this.trial			= trial;
		this.answerString	= answerString;
		this.correct		= correct;
		LogItem.debug("Initialized new wordcheck '" + word.getName() + "' (" + correct + ")");
	}
	
	public static final void createTable()
	{
		ConnectionDetails.getInstance().execute(
			"CREATE TABLE IF NOT EXISTS wordcheck(" + "word_id INTEGER, " + "trial_id INTEGER, " + "answerString VARCHAR(200), " + "correct INTEGER, " + "PRIMARY KEY(word_id, trial_id), "
				+ "FOREIGN KEY(word_id) REFERENCES word(word_id) ON UPDATE CASCADE ON DELETE CASCADE, " + "FOREIGN KEY(trial_id) REFERENCES trial(trial_id) ON UPDATE CASCADE ON DELETE CASCADE)");
		LogItem.debug("Wordcheck table created");
	}
	
	public static final void clearCache()
	{
		WordCheck.cache.clear();
		LogItem.debug("Cleared wordcheck cache");
	}
	
	public static final WordCheck get(final Word word, final Trial trial)
	{
		final Integer	word_id		= word.getWord_id();
		final Integer	trial_id	= trial.getTrial_id();
		final int		hash		= WordCheck.createKeyHash(word_id, trial_id);
		if (WordCheck.cache.containsKey(hash))
		{
			WordCheck wc = WordCheck.cache.get(hash);
			return wc;
		}
		WordCheck wc = WordCheck.readEntity(word_id, trial_id);
		return wc;
	}
	
	public static final WordCheck createWordCheck(final Word word, final Trial trial, final String answerString, final Boolean correct)
	{
		WordCheck wc = WordCheck.get(word, trial);
		if (wc == null)
		{
			wc = new WordCheck(word, trial, answerString, correct);
			WordCheck.writeEntity(wc);
			WordCheck.cache.put(WordCheck.createKeyHash(word.getWord_id(), trial.getTrial_id()), wc);
		}
		TrialComponent.repopulateAllTrials();
		return wc;
	}
	
	public static final void removeWordCheck(final Word word, final Trial trial)
	{
		WordCheck.cache.remove(WordCheck.createKeyHash(word.getWord_id(), trial.getTrial_id()));
		final String		query	= "DELETE FROM wordcheck " + "WHERE word_id = ? " + "AND trial_id = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word.getWord_id());
			pstmt.setInt(2, trial.getTrial_id());
			pstmt.execute();
			LogItem.debug("Wordcheck for word '" + word.getName() + "' and trial at " + Trial.getDateFormatter().format(trial.getDate()) + " removed");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static final void removeAllWordChecks()
	{
		WordCheck.clearCache();
		ConnectionDetails.getInstance().execute("DELETE FROM wordcheck");
		LogItem.debug("All wordchecks removed");
	}
	
	private static final WordCheck readEntity(final Integer word_id, final Integer trial_id)
	{
		final String		query	= "SELECT * " + "FROM wordcheck " + "WHERE word_id = ? " + "AND trial_id = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word_id);
			pstmt.setInt(2, trial_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final String	answerString	= rs.getString("answerString");
				final Boolean	correct			= rs.getInt("correct") == 0 ? false : true;
				final WordCheck	c				= new WordCheck(Word.get(word_id), Trial.get(trial_id), answerString, correct);
				WordCheck.cache.put(WordCheck.createKeyHash(word_id, trial_id), c);
				rs.close();
				return c;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static final void writeEntity(final WordCheck check)
	{
		final String		query	= "INSERT INTO wordcheck " + "VALUES(?, ?, ?, ?)";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, check.getWord().getWord_id());
			pstmt.setInt(2, check.getTrial().getTrial_id());
			pstmt.setString(3, check.getAnswerString());
			pstmt.setInt(4, check.isCorrect() ? 1 : 0);
			pstmt.executeUpdate();
			LogItem.debug("Inserted new wordcheck entity " + check.getWord() + " (" + check.isCorrect() + ") at " + Trial.getDateFormatter().format(check.getTrial().getDate()));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final Integer createKeyHash(Integer k1, Integer k2)
	{
		if (k1 > k2)
		{
			final Integer temp = k1;
			k1	= k2;
			k2	= temp;
		}
		final Integer hash = ((k1 + k2) * (k1 + k2 + 1)) / 2 + k2;
		return hash;
	}
	
	public final Word getWord()
	{
		return this.word;
	}
	
	public final Trial getTrial()
	{
		return this.trial;
	}
	
	public final String getAnswerString()
	{
		return this.answerString;
	}
	
	public final Boolean isCorrect()
	{
		return this.correct;
	}
}
