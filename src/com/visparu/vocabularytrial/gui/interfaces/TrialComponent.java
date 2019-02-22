package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public interface TrialComponent
{
	
	List<TrialComponent> instances = new ArrayList<>();
	
	static void repopulateAllTrials()
	{
		LogItem.enter();
		TrialComponent.instances.forEach(i -> i.repopulateTrials());
		LogItem.exit();
	}
	
	void repopulateTrials();
	
}
