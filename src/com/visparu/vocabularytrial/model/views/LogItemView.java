package com.visparu.vocabularytrial.model.views;

import java.time.format.DateTimeFormatter;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public class LogItemView
{

	private final LogItem logitem;
	
	public LogItemView(final LogItem logitem)
	{
		this.logitem = logitem;
	}
	
	public Integer getLogitem_id()
	{
		return this.logitem.getLogitem_id();
	}
	
	public Integer getLog_id()
	{
		return this.logitem.getLog_id();
	}
	
	public String getDatetime()
	{
		return this.logitem.getDatetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
	
	public String getSeverity()
	{
		return this.logitem.getSeverity().toString();
	}
	
	public String getThread()
	{
		return this.logitem.getThreadName();
	}
	
	public String getFunction()
	{
		return this.logitem.getFunction();
	}
	
	public String getMessage()
	{
		return this.logitem.getMessage();
	}
	
	public String getDescription()
	{
		return this.logitem.getDescription();
	}
	
}
