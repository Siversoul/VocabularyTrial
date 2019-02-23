package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.util.GUIUtil;
import com.visparu.vocabularytrial.util.I18N;

import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public final class TrialController implements Initializable, VokAbfController
{
	@FXML
	private TextArea	ta_question;
	@FXML
	private TextArea	ta_answer;
	@FXML
	private TextArea	ta_solution;
	@FXML
	private Button		bt_correct;
	@FXML
	private Button		bt_wrong;
	@FXML
	private Button		bt_solution;
	
	private enum State
	{
		INIT, QUESTION, SOLUTION
	}
	
	private Stage stage;
	
	private final Language language_to;
	
	private final Trial trial;
	
	private final List<Word>	words;
	private int					currentIndex	= 0;
	
	private State currentState = State.INIT;
	
	public TrialController(final Language language_from, final Language language_to, final List<Word> words)
	{
		LogItem.enter();
		this.language_to	= language_to;
		this.words			= words;
		
		this.trial = Trial.createTrial(Date.from(Instant.now()), language_from, language_to);
		LogItem.exit();
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.enter();
		LogItem.debug("Initializing new stage with TrialController");
		VokAbfController.instances.add(this);
		this.stage.setOnCloseRequest(e ->
		{
			LogItem.enter();
			VokAbfController.instances.remove(this);
			
			if (this.trial.getWordChecks().isEmpty())
			{
				LogItem.exit();
				return;
			}
			final TrialResultController	trc		= new TrialResultController(this.trial);
			final StringBinding			title	= I18N.createStringBinding("gui.result.title");
			GUIUtil.createNewStage("TrialResult", trc, title);
			LogItem.exit();
		});
		
		this.bt_correct.setTooltip(new Tooltip(I18N.createStringBinding("gui.trial.correct.tooltip").get()));
		this.bt_wrong.setTooltip(new Tooltip(I18N.createStringBinding("gui.trial.wrong.tooltip").get()));
		
		this.ta_answer.requestFocus();
		
		this.cycle(State.QUESTION);
		LogItem.debug("Finished initializing new stage");
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
		this.stage.setOnCloseRequest(null);
		this.stage.close();
		LogItem.debug("Stage closed");
		LogItem.exit();
	}
	
	@FXML
	public final void exit(final ActionEvent event)
	{
		LogItem.enter();
		this.close();
		LogItem.exit();
	}
	
	@FXML
	public final void correct(final ActionEvent event)
	{
		LogItem.enter();
		final Word word = this.words.get(this.currentIndex);
		WordCheck.createWordCheck(word, this.trial, this.ta_answer.getText(), true);
		LogItem.debug("Created correct WordCheck for word " + word.getName());
		this.cycle(State.QUESTION);
		LogItem.exit();
	}
	
	@FXML
	public final void wrong(final ActionEvent event)
	{
		LogItem.enter();
		final Word word = this.words.get(this.currentIndex);
		WordCheck.createWordCheck(word, this.trial, this.ta_answer.getText(), false);
		LogItem.debug("Created wrong WordCheck for word " + word.getName());
		this.cycle(State.QUESTION);
		LogItem.exit();
	}
	
	@FXML
	public final void solution(final ActionEvent event)
	{
		LogItem.enter();
		LogItem.debug("Showing solution");
		this.cycle(State.SOLUTION);
		LogItem.exit();
	}
	
	@FXML
	public final void keyPressed(final KeyEvent event)
	{
		LogItem.enter();
		if (event.getCode() == KeyCode.ESCAPE)
		{
			this.exit(null);
			LogItem.exit();
			return;
		}
		if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.ENTER)
		{
			this.solution(null);
			LogItem.exit();
			return;
		}
		if (event.isControlDown() && event.getCode() == KeyCode.ENTER)
		{
			this.correct(null);
			LogItem.exit();
			return;
		}
		if (event.isControlDown() && event.getCode() == KeyCode.BACK_SPACE)
		{
			this.wrong(null);
			LogItem.exit();
			return;
		}
	}
	
	private final void cycle(final State nextState)
	{
		LogItem.enter();
		if (this.currentState == State.INIT)
		{
			LogItem.debug("Cycling from INIT to QUESTION");
			this.currentState = State.QUESTION;
			this.setQuestion(this.words.get(this.currentIndex));
			LogItem.exit();
			return;
		}
		
		LogItem.debug("Cycling from " + this.currentState.name() + " to " + nextState.name());
		switch (nextState)
		{
			case QUESTION:
			{
				this.currentIndex++;
				if (this.currentIndex >= this.words.size())
				{
					LogItem.info("Trial completed");
					this.exit(null);
					LogItem.exit();
					return;
				}
				this.ta_answer.setEditable(true);
				this.bt_solution.setDisable(false);
				this.setSolution(null);
				this.setQuestion(this.words.get(this.currentIndex));
				this.ta_answer.setText("");
				break;
			}
			case SOLUTION:
			{
				this.ta_answer.setEditable(false);
				this.bt_solution.setDisable(true);
				
				this.setSolution(this.words.get(this.currentIndex));
				
				break;
			}
			default:
			{
				LogItem.exit();
				throw new IllegalStateException();
			}
		}
		this.currentState = nextState;
		LogItem.debug("State switched");
		LogItem.exit();
	}
	
	private final void setQuestion(final Word question)
	{
		LogItem.enter();
		this.ta_question.setText(question.getName());
		LogItem.exit();
	}
	
	private final void setSolution(final Word question)
	{
		LogItem.enter();
		if (question == null)
		{
			this.ta_solution.setText("");
		}
		else
		{
			final List<Translation>	translations	= question.getTranslations(this.language_to);
			final StringBuilder		sb				= new StringBuilder();
			for (int i = 0; i < translations.size(); i++)
			{
				final Translation t = translations.get(i);
				if (i != 0)
				{
					sb.append("\n");
				}
				final String name;
				if (question.getWord_id() == t.getWord1_id())
				{
					name = t.getWord2().getName();
				}
				else
				{
					name = t.getWord1().getName();
				}
				sb.append(name);
			}
			this.ta_solution.setText(sb.toString());
		}
		LogItem.exit();
	}
}
