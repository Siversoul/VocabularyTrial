package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface WordComponent
{
	
	List<WordComponent> instances = new ArrayList<>();
	
	static void repopulateAllWords()
	{
		WordComponent.instances.forEach(i -> i.repopulateWords());
	}
	
	void repopulateWords();
	
}
