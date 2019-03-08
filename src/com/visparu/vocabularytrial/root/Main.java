package com.visparu.vocabularytrial.root;

import java.net.URL;

import com.visparu.vocabularytrial.gui.controllers.MainMenuController;
import com.visparu.vocabularytrial.model.db.Database;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.log.Severity;
import com.visparu.vocabularytrial.util.C11N;
import com.visparu.vocabularytrial.util.I18N;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application
{
	public static final String	NAME			= I18N.createStringBinding("root.name").get();
	public static final String	VERSION			= "0.3.1";
	public static final String	AUTHOR			= "Oliver Stiller";
	public static final String	RELEASE_DATE	= "05.03.2019";
	
	@Override
	public final void start(final Stage primaryStage)
	{
		this.initializeDatabase();
		LogItem.initializeNewLogSession();
		LogItem.createLogItem(Severity.INFO, "Initialized database and log");
		this.initializeStage(primaryStage);
	}
	
	private final void initializeDatabase()
	{
		Database.get().activateForeignKeyPragma();
		Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
	}
	
	private final void initializeStage(final Stage primaryStage)
	{
		try
		{
			final URL					url		= this.getClass().getResource("/com/visparu/vocabularytrial/gui/fxml/MainMenu.fxml");
			final FXMLLoader			loader	= new FXMLLoader(url);
			final MainMenuController	mmc		= new MainMenuController(primaryStage);
			loader.setController(mmc);
			loader.setResources(I18N.getResources());
			final Parent	root	= loader.load();
			final Scene		scene	= new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.title"));
			primaryStage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		LogItem.debug("Main stage initialized");
	}
	
	public static final void main(String[] args)
	{
		launch(args);
	}
}
