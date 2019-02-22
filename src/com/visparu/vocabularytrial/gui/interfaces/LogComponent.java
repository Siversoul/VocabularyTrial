package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public interface LogComponent
{
	
	List<LogComponent> instances = new ArrayList<>();
	
	static void repopulateAllLogs()
	{
		LogItem.enter();
		LogComponent.instances.forEach(i -> i.repopulateLogs());
		LogItem.exit();
	}
	
	void repopulateLogs();
	
}
