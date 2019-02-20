package com.visparu.vocabularytrial.model.views;

import java.text.SimpleDateFormat;

import com.visparu.vocabularytrial.model.db.entities.Trial;

public final class TrialView
{

	private final Trial trial;

	public TrialView(final Trial trial)
	{
		this.trial = trial;
	}

	public final Integer getTrial_id()
	{
		return this.trial.getTrial_id();
	}

	public final String getDate()
	{
		return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(this.trial.getDate());
	}

	public final String getCount()
	{
		return String.valueOf(this.trial.getWordChecks().size());
	}

	public final String getCorrect()
	{
		return String.valueOf(this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect()).count());
	}

	public final String getWrong()
	{
		return String.valueOf(this.trial.getWordChecks().stream().filter(wc -> !wc.isCorrect()).count());
	}

	public final String getPercentage()
	{
		final long correct = this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect()).count();
		final int count = this.trial.getWordChecks().size();
		return String.format("%.2f", (double) correct / count);
	}

}
