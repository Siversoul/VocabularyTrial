package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface LanguageComponent
{
	
	List<LanguageComponent> instances = new ArrayList<>();
	
	static void repopulateAllLanguages()
	{
		LanguageComponent.instances.forEach(i -> i.repopulateLanguages());
	}
	
	void repopulateLanguages();
	
}
