package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;

public final class Language
{

	private static final Map<String, Language>	cache	= new HashMap<>();

	private String								language_code;
	private String								name;

	private Language(final String language_code, final String name)
	{
		this.language_code = language_code;
		this.name = name;
	}

	@Override
	public final String toString()
	{
		return this.name;
	}

	public final static void createTable()
	{
		ConnectionDetails.getInstance().executeSimpleStatement("CREATE TABLE IF NOT EXISTS language (language_code VARCHAR(2) PRIMARY KEY, name VARCHAR(30))");
	}

	public final static void clearCache()
	{
		Language.cache.clear();
	}

	public final static Language get(final String language_code)
	{
		if (Language.cache.containsKey(language_code))
		{
			return Language.cache.get(language_code);
		}
		return Language.readEntity(language_code);
	}

	public final static List<Language> getAll()
	{
		final List<Language> languages = new ArrayList<>();
		final String query = "SELECT language_code FROM language";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final Statement stmt = conn.createStatement())
		{
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
			{
				final String language_code = rs.getString("language_code");
				final Language language = Language.get(language_code);
				if (language != null)
				{
					languages.add(language);
				}
			}
			rs.close();
			return languages;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public final static Language createLanguage(final String language_code, final String name)
	{
		if (Language.get(language_code) != null)
		{
			throw new IllegalArgumentException("Language with language_code '" + language_code + "' already exists!");
		}

		Language l = Language.get(language_code);
		if (l == null)
		{
			l = new Language(language_code, name);
			Language.writeEntity(l);
			Language.cache.put(language_code, l);
		}
		LanguageComponent.repopulateAllLanguages();
		return l;
	}

	public final static void removeLanguage(final String language_code)
	{
		Language.cache.remove(language_code);
		final String query = "DELETE FROM language WHERE language_code = ?";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, language_code);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LanguageComponent.repopulateAllLanguages();
	}

	public final static void removeAllLanguages()
	{
		Language.clearCache();
		ConnectionDetails.getInstance().executeSimpleStatement("DELETE FROM language");
		LanguageComponent.repopulateAllLanguages();
	}

	private final static Language readEntity(final String language_code)
	{
		final String query = "SELECT * FROM language WHERE language_code = ?";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, language_code);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final String name = rs.getString("name");
				final Language l = new Language(language_code, name);
				Language.cache.put(language_code, l);
				rs.close();
				return l;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private final static void writeEntity(final Language language)
	{
		final String query = "INSERT INTO language VALUES(?, ?)";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, language.getLanguage_code());
			pstmt.setString(2, language.getName());
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public final String getLanguage_code()
	{
		return this.language_code;
	}

	public final void setLanguage_code(final String language_code)
	{
		final String query = "UPDATE language SET language_code = ? WHERE language_code = ?";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, language_code);
			pstmt.setString(2, this.language_code);
			pstmt.executeUpdate();
			Language.cache.remove(this.language_code);
			Language.cache.put(language_code, this);
			this.language_code = language_code;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public final String getName()
	{
		return this.name;
	}

	public final void setName(final String name)
	{
		final String query = "UPDATE language SET name = ? WHERE language_code = ?";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, name);
			pstmt.setString(2, this.language_code);
			pstmt.executeUpdate();
			this.name = name;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public final List<Word> getWords()
	{
		final List<Word> words = new ArrayList<>();
		final String query = "SELECT * FROM word WHERE language_code = ? ORDER BY word.name";
		final String connString = ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
				final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, this.language_code);
			final ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				final String word_name = rs.getString("name");
				final Word word = Word.get(word_name, this);
				if (word != null)
				{
					words.add(word);
				}
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return words;
	}

}
