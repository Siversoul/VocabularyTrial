package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.LogComponent;
import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.ConnectionDetails;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.log.Severity;
import com.visparu.vocabularytrial.model.views.LogItemView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public final class LogController implements Initializable, VokAbfController, LogComponent
{
	@FXML
	private ComboBox<String>					cb_severity;
	@FXML
	private ComboBox<String>					cb_thread;
	@FXML
	private ComboBox<String>					cb_function;
	@FXML
	private TextField							tf_search;
	@FXML
	private CheckBox							cb_includedescription;
	@FXML
	private TableView<LogItemView>				tv_log;
	@FXML
	private TableColumn<LogItemView, String>	tc_time;
	@FXML
	private TableColumn<LogItemView, String>	tc_thread;
	@FXML
	private TableColumn<LogItemView, String>	tc_function;
	@FXML
	private TableColumn<LogItemView, String>	tc_message;
	
	private Stage stage;
	
	@Override
	public final void initialize(URL location, ResourceBundle resources)
	{
		this.cb_severity.getItems().add("All");
		for (Severity s : Severity.values())
		{
			this.cb_severity.getItems().add(s.toString());
		}
		this.cb_severity.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateThreads();
		});
		this.cb_severity.getSelectionModel().select("All");
		
		this.tc_time.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("datetime"));
		this.tc_thread.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("thread"));
		this.tc_function.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("function"));
		this.tc_message.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("message"));
		
		Callback<TableColumn<LogItemView, String>, TableCell<LogItemView, String>> cb = (c ->
		{
			return new TableCell<LogItemView, String>()
			{
				@Override
				protected void updateItem(String s, boolean empty)
				{
					LogItemView	liv	= this.getTableRow().getItem();
					Color		c;
					switch (liv.getSeverity())
					{
						case "Trace":
						{
							c = Color.WHITE;
							break;
						}
						case "Debug":
						{
							c = Color.GREY;
							break;
						}
						case "Info":
						{
							c = Color.LIGHTGREEN;
							break;
						}
						case "Warning":
						{
							c = Color.YELLOW;
							break;
						}
						case "Error":
						{
							c = Color.ORANGE;
							break;
						}
						case "Critical":
						{
							c = Color.RED;
							break;
						}
						default:
						{
							c = Color.ALICEBLUE;
							break;
						}
					}
					this.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
				}
			};
		});
		
		this.tc_time.setCellFactory(cb);
		this.tc_thread.setCellFactory(cb);
		this.tc_function.setCellFactory(cb);
		this.tc_message.setCellFactory(cb);
		
		this.repopulateLogs();
	}
	
	private final void repopulateThreads()
	{
		this.cb_thread.getItems().clear();
		this.cb_thread.getItems().add("All");
		final String	query_thread		= "SELECT DISTINCT thread FROM logitem";
		final String	connString_thread	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString_thread); final Statement stmt = conn.createStatement())
		{
			final ResultSet rs = stmt.executeQuery(query_thread);
			while (rs.next())
			{
				this.cb_thread.getItems().add(rs.getString("thread"));
			}
			rs.close();
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		this.repopulateFunctions();
	}
	
	private final void repopulateFunctions()
	{
		this.cb_function.getItems().clear();
		this.cb_function.getItems().add("All");
		final String	query_func		= "SELECT DISTINCT function FROM logitem";
		final String	connString_func	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString_func); final Statement stmt = conn.createStatement())
		{
			ResultSet rs = stmt.executeQuery(query_func);
			while (rs.next())
			{
				this.cb_function.getItems().add(rs.getString("function"));
			}
			rs.close();
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public final void repopulateLogs()
	{
		this.filter(null);
	}
	
	@Override
	public final void setStage(Stage stage)
	{
		this.stage = stage;
	}
	
	@Override
	public final void close()
	{
		this.stage.close();
	}
	
	@FXML
	public final void filter(ActionEvent event)
	{
		Severity severity = Severity.valueOf(this.cb_severity.getSelectionModel().getSelectedItem());
		String thread = this.cb_thread.getSelectionModel().getSelectedItem();
		if(thread.equals("All"))
		{
			thread = null;
		}
		String function = this.cb_function.getSelectionModel().getSelectedItem();
		if(function.equals("All"))
		{
			function = null;
		}
		String message = this.tf_search.getText();
		if(message.isEmpty())
		{
			message = null;
		}
		boolean description = this.cb_includedescription.isSelected();
		final List<LogItem>					logitems		= LogItem.getFilteredLogItems(LogItem.getSessionLog_id(), severity, thread, function, message, description);
		final ObservableList<LogItemView>	logitemviews	= FXCollections.observableArrayList();
		logitems.forEach(li -> logitemviews.add(new LogItemView(li)));
		this.tv_log.setItems(logitemviews);
	}
	
	@FXML
	public final void close(ActionEvent event)
	{
		this.close();
	}
}
