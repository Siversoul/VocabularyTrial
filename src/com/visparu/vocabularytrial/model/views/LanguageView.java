package com.visparu.vocabularytrial.model.views;

public final class LanguageView
{
	
	private String	language_code;
	private String	name;
	
	public LanguageView(final String language_code, final String name)
	{
		this.language_code	= language_code;
		this.name			= name;
	}
	
	public final String getLanguage_code()
	{
		return this.language_code;
	}
	
	public final void setLanguage_code(final String language_code)
	{
		this.language_code = language_code;
	}
	
	public final String getName()
	{
		return this.name;
	}
	
	public final void setName(final String name)
	{
		this.name = name;
	}
	
}
