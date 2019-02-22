package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface TrialComponent
{
	
	List<TrialComponent> instances = new ArrayList<>();
	
	static void repopulateAllTrials()
	{
		TrialComponent.instances.forEach(i -> i.repopulateTrials());
	}
	
	void repopulateTrials();
	
}
