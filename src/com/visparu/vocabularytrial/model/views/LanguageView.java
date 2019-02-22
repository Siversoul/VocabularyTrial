package com.visparu.vocabularytrial.model.views;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public final class LanguageView
{
	
	private String	language_code;
	private String	name;
	
	public LanguageView(final String language_code, final String name)
	{
		LogItem.enter();
		this.language_code	= language_code;
		this.name			= name;
		LogItem.exit();
	}
	
	public final String getLanguage_code()
	{
		LogItem.enter();
		LogItem.exit();
		return this.language_code;
	}
	
	public final void setLanguage_code(final String language_code)
	{
		LogItem.enter();
		this.language_code = language_code;
		LogItem.exit();
	}
	
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
	
}
