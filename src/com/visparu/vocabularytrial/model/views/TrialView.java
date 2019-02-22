package com.visparu.vocabularytrial.model.views;

import java.text.SimpleDateFormat;

import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Trial;

public final class TrialView
{
	
	private final Trial trial;
	
	public TrialView(final Trial trial)
	{
		LogItem.enter();
		this.trial = trial;
		LogItem.exit();
	}
	
	public final Integer getTrial_id()
	{
		LogItem.enter();
		Integer trial_id = this.trial.getTrial_id();
		LogItem.exit();
		return trial_id;
	}
	
	public final String getDate()
	{
		LogItem.enter();
		String dateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(this.trial.getDate());
		LogItem.exit();
		return dateString;
	}
	
	public final String getCount()
	{
		LogItem.enter();
		String count = String.valueOf(this.trial.getWordChecks().size());
		LogItem.exit();
		return count;
	}
	
	public final String getCorrect()
	{
		LogItem.enter();
		String correct = String.valueOf(this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect()).count());
		LogItem.exit();
		return correct;
	}
	
	public final String getWrong()
	{
		LogItem.enter();
		String wrong = String.valueOf(this.trial.getWordChecks().stream().filter(wc -> !wc.isCorrect()).count());
		LogItem.exit();
		return wrong;
	}
	
	public final String getPercentage()
	{
		LogItem.enter();
		final long	correct	= this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect()).count();
		final int	count	= this.trial.getWordChecks().size();
		String perc = String.format("%.2f", (double) correct / count);
		LogItem.exit();
		return perc;
	}
	
}
