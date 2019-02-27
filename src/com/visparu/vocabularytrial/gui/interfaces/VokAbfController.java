package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

import javafx.stage.Stage;

public interface VokAbfController
{
	
	List<VokAbfController> instances = new ArrayList<>();
	
	static void repopulateAll()
	{
		LogItem.enter();
		LanguageComponent.repopulateAllLanguages();
		WordComponent.repopulateAllWords();
		TrialComponent.repopulateAllTrials();
		LogComponent.repopulateAllLogs();
		LogItem.debug("All components repopulated");
		LogItem.exit();
	}
	
	static void closeAll()
	{
		LogItem.enter();
		while (!VokAbfController.instances.isEmpty())
		{
			VokAbfController.instances.get(0).close();
		}
		LogItem.debug("All stages closed");
		LogItem.exit();
	}
	
	void setStage(Stage stage);
	
	void close();
	
}
