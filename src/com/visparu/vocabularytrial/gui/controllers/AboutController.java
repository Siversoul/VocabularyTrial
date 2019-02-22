package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.root.Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public final class AboutController implements Initializable, VokAbfController
{
	@FXML
	private Label	lb_name;
	@FXML
	private Label	lb_version;
	@FXML
	private Label	lb_author;
	@FXML
	private Label	lb_date;
	
	private Stage stage;
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.enter();
		VokAbfController.instances.add(this);
		this.stage.setOnCloseRequest(e ->
		{
			LogItem.enter();
			VokAbfController.instances.remove(this);
			LogItem.exit();
		});
		
		this.lb_name.setText(Main.NAME);
		this.lb_version.setText(Main.VERSION);
		this.lb_author.setText(Main.AUTHOR);
		this.lb_date.setText(Main.RELEASE_DATE);
		LogItem.exit();
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		LogItem.enter();
		this.stage = stage;
		LogItem.exit();
	}
	
	@Override
	public final void close()
	{
		LogItem.enter();
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.exit();
	}
	
	@FXML
	public final void close(final ActionEvent event)
	{
		LogItem.enter();
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.exit();
	}
}
