package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.gui.interfaces.WordComponent;
import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.templates.WordTemplate;
import com.visparu.vocabularytrial.util.I18N;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public final class AddWordsController implements Initializable, LanguageComponent, VokAbfController
{
	@FXML
	private TextField							tf_word;
	@FXML
	private TextField							tf_translations;
	@FXML
	private TableView<WordTemplate>				tv_vocabulary;
	@FXML
	private TableColumn<WordTemplate, String>	tc_word;
	@FXML
	private TableColumn<WordTemplate, String>	tc_translations;
	@FXML
	private ChoiceBox<Language>					cb_language_from;
	@FXML
	private ChoiceBox<Language>					cb_language_to;
	
	private Stage			stage;
	private final Language	init_l_from;
	private final Language	init_l_to;
	
	public AddWordsController(final Language l_from, final Language l_to)
	{
		LogItem.enter();
		this.init_l_from	= l_from;
		this.init_l_to		= l_to;
		LogItem.exit();
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.enter();
		LanguageComponent.instances.add(this);
		VokAbfController.instances.add(this);
		this.stage.setOnCloseRequest(e ->
		{
			LogItem.enter();
			LanguageComponent.instances.remove(this);
			VokAbfController.instances.remove(this);
			LogItem.exit();
		});
		
		this.tc_word.setCellValueFactory(new PropertyValueFactory<WordTemplate, String>("name"));
		this.tc_translations.setCellValueFactory(new PropertyValueFactory<WordTemplate, String>("translationsString"));
		
		this.repopulateLanguages_from();
		this.cb_language_from.getSelectionModel().select(this.init_l_from);
		this.repopulateLanguages_to();
		this.cb_language_to.getSelectionModel().select(this.init_l_to);
		this.cb_language_from.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			LogItem.enter();
			this.repopulateLanguages_to();
			LogItem.exit();
		});
		LogItem.exit();
	}
	
	@Override
	public final void repopulateLanguages()
	{
		LogItem.enter();
		this.repopulateLanguages_from();
		this.repopulateLanguages_to();
		LogItem.exit();
	}
	
	private final void repopulateLanguages_from()
	{
		LogItem.enter();
		this.cb_language_from.setItems(FXCollections.observableArrayList(Language.getAll()));
		LogItem.exit();
	}
	
	private final void repopulateLanguages_to()
	{
		LogItem.enter();
		final Language l_prev = this.cb_language_to.getSelectionModel().getSelectedItem();
		this.cb_language_to.setItems(FXCollections.observableArrayList(Language.getAll()));
		this.cb_language_to.getItems().remove(this.cb_language_from.getSelectionModel().getSelectedItem());
		if (this.cb_language_to.getItems().contains(l_prev))
		{
			this.cb_language_to.getSelectionModel().select(l_prev);
		}
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
	public final void switchFocus(final ActionEvent event)
	{
		LogItem.enter();
		this.tf_translations.requestFocus();
		LogItem.exit();
	}
	
	@FXML
	public final void addWord(final ActionEvent event)
	{
		LogItem.enter();
		final WordTemplate wt = new WordTemplate();
		wt.setName(this.tf_word.getText());
		wt.setTranslationsString(this.tf_translations.getText());
		this.tv_vocabulary.getItems().add(wt);
		
		this.tf_word.setText("");
		this.tf_translations.setText("");
		
		this.tf_word.requestFocus();
		LogItem.exit();
	}
	
	@FXML
	public final void confirm(final ActionEvent event)
	{
		LogItem.enter();
		final Language	l_from	= this.cb_language_from.getValue();
		final Language	l_to	= this.cb_language_to.getValue();
		if (l_from == null || l_to == null)
		{
			final Alert alert = new Alert(AlertType.ERROR, I18N.createStringBinding("gui.addwords.alert.languages").get(),
				ButtonType.OK);
			alert.showAndWait();
			LogItem.exit();
			return;
		}
		final List<WordTemplate> wordTemplates = this.tv_vocabulary.getItems();
		for (final WordTemplate wt : wordTemplates)
		{
			final String	name	= wt.getName();
			Word			w		= Word.get(name, l_from);
			if (w == null)
			{
				w = Word.createWord(name, l_from);
			}
			final String[] translationNames = wt.getTranslationsString().split("[,|;|/]");
			for (final String tn : translationNames)
			{
				final String	tn_sane	= tn.trim();
				Word			tw		= Word.get(tn_sane, l_to);
				if (tw == null)
				{
					tw = Word.createWord(tn_sane, l_to);
				}
				final Translation t = Translation.get(w, tw);
				if (t == null)
				{
					Translation.createTranslation(w, tw);
				}
			}
		}
		WordComponent.repopulateAllWords();
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.exit();
	}
	
	@FXML
	public final void cancel(final ActionEvent event)
	{
		LogItem.enter();
		if (!this.tv_vocabulary.getItems().isEmpty())
		{
			final Alert					alert	= new Alert(AlertType.WARNING, I18N.createStringBinding("gui.addwords.alert.unsaved").get(),
				ButtonType.YES,
				ButtonType.NO);
			final Optional<ButtonType>	result	= alert.showAndWait();
			if (!result.isPresent() || result.get() != ButtonType.YES)
			{
				LogItem.exit();
				return;
			}
		}
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.exit();
	}
}
