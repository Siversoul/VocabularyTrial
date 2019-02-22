package com.visparu.vocabularytrial.model.templates;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public final class WordTemplate
{
	
	private String	name;
	private String	translationsString;
	
	public final String getName()
	{
		LogItem.enter();
		LogItem.exit();
		return this.name;
	}
	
	public final void setName(final String name)
	{
		LogItem.enter();
		this.name = name;
		LogItem.exit();
	}
	
	public final String getTranslationsString()
	{
		LogItem.enter();
		LogItem.exit();
		return this.translationsString;
	}
	
	public final void setTranslationsString(final String translationsString)
	{
		LogItem.enter();
		this.translationsString = translationsString;
		LogItem.exit();
	}
	
}
