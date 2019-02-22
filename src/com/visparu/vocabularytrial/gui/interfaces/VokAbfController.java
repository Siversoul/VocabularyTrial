package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.Stage;

public interface VokAbfController
{
	
	List<VokAbfController> instances = new ArrayList<>();
	
	static void repopulateAll()
	{
		LanguageComponent.repopulateAllLanguages();
		WordComponent.repopulateAllWords();
		TrialComponent.repopulateAllTrials();
	}
	
	static void closeAll()
	{
		while (!VokAbfController.instances.isEmpty())
		{
			VokAbfController.instances.get(0).close();
		}
	}
	
	void setStage(Stage stage);
	
	void close();
	
}
