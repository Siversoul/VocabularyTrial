package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public final class AddLanguageController implements Initializable, VokAbfController
{
	@FXML
	private TextField								tf_language_code;
	@FXML
	private TextField								tf_language;
	public static final List<AddLanguageController>	instances	= new ArrayList<>();
	private Stage									stage;
	
	@Override
	public void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with AddLanguageController");
		VokAbfController.instances.add(this);
		AddLanguageController.instances.add(this);
		this.stage.setOnCloseRequest(e ->
		{
			VokAbfController.instances.remove(this);
			AddLanguageController.instances.remove(this);
		});
		LogItem.debug("Finished initializing new stage");
	}
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.debug("Stage closed");
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		this.stage = stage;
	}
	
	@FXML
	public final void confirm(final ActionEvent event)
	{
		Language.createLanguage(this.tf_language_code.getText(), this.tf_language.getText());
		LogItem.info("Language " + this.tf_language + " created");
		this.close();
	}
	
	@FXML
	public final void cancel(final ActionEvent event)
	{
		this.close();
	}
}
