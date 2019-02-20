package com.visparu.vocabularytrial.model.views;

import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Word;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public final class CheckView
{

	private final Word word;
	private final String answerString;
	private final BooleanProperty correct;
	private final Language language_to;

	public CheckView(final Word word, final String answerString, final boolean correct, final Language language_to)
	{
		this.word = word;
		this.answerString = answerString;
		this.correct = new SimpleBooleanProperty(correct);
		this.language_to = language_to;
	}

	public final String getName()
	{
		return this.word.getName();
	}

	public final String getAnswerString()
	{
		return this.answerString;
	}

	public final String getTranslationString()
	{
		final List<Translation> tlist = this.word.getTranslations(this.language_to);
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tlist.size(); i++)
		{
			final Translation t = tlist.get(i);
			if(i != 0)
			{
				sb.append(", ");
			}
			final String name;
			if(this.word.getWord_id() == t.getWord1_id())
			{
				name = t.getWord2().getName();
			}
			else
			{
				name = t.getWord1().getName();
			}
			sb.append(name);
		}
		return sb.toString();
	}

	public final BooleanProperty correctProperty()
	{
		return this.correct;
	}

}
