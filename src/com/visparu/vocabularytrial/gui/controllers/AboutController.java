package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
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
		VokAbfController.instances.add(this);
		this.stage.setOnCloseRequest(e ->
		{
			VokAbfController.instances.remove(this);
		});
		
		this.lb_name.setText(Main.NAME);
		this.lb_version.setText(Main.VERSION);
		this.lb_author.setText(Main.AUTHOR);
		this.lb_date.setText(Main.RELEASE_DATE);
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		this.stage = stage;
	}
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
	}
	
	@FXML
	public final void close(final ActionEvent event)
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
	}
}
