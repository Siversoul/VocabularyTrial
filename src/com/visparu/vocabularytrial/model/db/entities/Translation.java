package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.visparu.vocabularytrial.model.db.ConnectionDetails;

public final class Translation
{
	private static final Map<Integer, Translation>	cache	= new HashMap<>();
	private final Integer							word1_id;
	private final Integer							word2_id;
	
	private Translation(Integer word1_id, Integer word2_id)
	{
		if (word1_id == null || word2_id == null || word1_id == -1 || word2_id == -1)
		{
			throw new IllegalArgumentException();
		}
		if (word1_id > word2_id)
		{
			final Integer temp = word1_id;
			word1_id	= word2_id;
			word2_id	= temp;
		}
		this.word1_id	= word1_id;
		this.word2_id	= word2_id;
		LogItem.debug("Initialized new translation '" + Word.get(word1_id).getName() + "/" + Word.get(word2_id).getName() + "'");
	}
	
	public final static void createTable()
	{
		ConnectionDetails.getInstance().execute("CREATE TABLE IF NOT EXISTS translation(" + "word1_id INTEGER, " + "word2_id INTEGER, " + "PRIMARY KEY(word1_id, word2_id), "
			+ "FOREIGN KEY(word1_id) REFERENCES word(word_id) ON UPDATE CASCADE, " + "FOREIGN KEY(word2_id) REFERENCES word(word_id) ON UPDATE CASCADE" + ")");
		LogItem.debug("Translation table created");
	}
	
	public final static void clearCache()
	{
		Translation.cache.clear();
		LogItem.debug("Cleared translation cache");
	}
	
	public final static Translation get(final Word w1, final Word w2)
	{
		final Integer	word1_id	= w1.getWord_id();
		final Integer	word2_id	= w2.getWord_id();
		final Integer	hash		= Translation.createKeyHash(word1_id, word2_id);
		if (Translation.cache.containsKey(hash))
		{
			Translation t = Translation.cache.get(hash);
			return t;
		}
		Translation t = Translation.readEntity(w1, w2);
		return t;
	}
	
	public final static Translation createTranslation(final Word w1, final Word w2)
	{
		if (w1 == null || w2 == null)
		{
			throw new IllegalArgumentException();
		}
		final Integer	word1_id	= w1.getWord_id();
		final Integer	word2_id	= w2.getWord_id();
		Translation		t			= Translation.get(w1, w2);
		if (t == null)
		{
			t = new Translation(word1_id, word2_id);
			Translation.writeEntity(t);
			Translation.cache.put(Translation.createKeyHash(word1_id, word2_id), t);
		}
		return t;
	}
	
	public final static void removeTranslation(final Word w1, final Word w2)
	{
		final Integer	word1_id	= w1.getWord_id();
		final Integer	word2_id	= w2.getWord_id();
		Translation.cache.remove(Translation.createKeyHash(word1_id, word2_id));
		final String		query	= "DELETE FROM translation " + "WHERE word1_id = ? " + "AND word2_id = ?";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word1_id);
			pstmt.setInt(2, word2_id);
			pstmt.executeUpdate();
			LogItem.debug("Translation '" + w1.getName() + "/" + w2.getName() + "' removed");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public final static void removeAllTranslations()
	{
		Translation.clearCache();
		ConnectionDetails.getInstance().execute("DELETE FROM translation");
		LogItem.debug("All translations removed");
	}
	
	public final static Translation readEntity(Word w1, Word w2)
	{
		final Integer		word1_id	= w1.getWord_id();
		final Integer		word2_id	= w2.getWord_id();
		final String		query		= "SELECT * " + "FROM translation " + "WHERE word1_id = ? " + "AND word2_id = ?";
		final Connection	conn		= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word1_id);
			pstmt.setInt(2, word2_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final Translation t = new Translation(word1_id, word2_id);
				Translation.cache.put(Translation.createKeyHash(word1_id, word2_id), t);
				rs.close();
				return t;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private final static void writeEntity(final Translation t)
	{
		final String		query	= "INSERT INTO translation " + "VALUES(?, ?)";
		final Connection	conn	= ConnectionDetails.getInstance().getConnection();
		try (final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, t.getWord1_id());
			pstmt.setInt(2, t.getWord2_id());
			pstmt.executeUpdate();
			LogItem.debug("Inserted new translation entity " + t.getWord1().getName() + "/" + t.getWord2().getName());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private final static Integer createKeyHash(Integer k1, Integer k2)
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
	
	public final Integer getWord1_id()
	{
		return this.word1_id;
	}
	
	public final Integer getWord2_id()
	{
		return this.word2_id;
	}
	
	public final Word getWord1()
	{
		Word w1 = Word.get(this.word1_id);
		return w1;
	}
	
	public final Word getWord2()
	{
		Word w2 = Word.get(this.word2_id);
		return w2;
	}
}
