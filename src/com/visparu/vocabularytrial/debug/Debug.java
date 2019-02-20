package com.visparu.vocabularytrial.debug;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.model.views.WordView;
import com.visparu.vocabularytrial.util.I18N;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public final class Debug
{

	private Debug()
	{

	}

	private static boolean showDebugWarning()
	{
		Alert alert = new Alert(AlertType.WARNING, I18N.createStringBinding("gui.debug.debugwarning").get(), ButtonType.YES, ButtonType.NO);
		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.YES;
	}

	public static final void debug_fillRandomly()
	{
		if(!Debug.showDebugWarning())
		{
			return;
		}

		final Random rand = new Random();
		for (int i = 0; i < 5; i++)
		{
			final StringBuilder lc_sb = new StringBuilder();
			for (int j = 0; j < 2; j++)
			{
				char c = (char) ('A' + rand.nextInt(26));
				lc_sb.append(c);
			}
			final StringBuilder ln_sb = new StringBuilder();
			for (int j = 0; j < 2; j++)
			{
				ln_sb.append(lc_sb.charAt(j));
				for (int k = 0; k < 4; k++)
				{
					char c = (char) ('a' + rand.nextInt(26));
					ln_sb.append(c);
				}
			}
			Language.createLanguage(lc_sb.toString(), ln_sb.toString());
		}

		final List<Language> l_list = Language.getAll();
		final List<Word> w_list = new ArrayList<>();
		for (int i = 0; i < 1000; i++)
		{
			final StringBuilder wn_sb = new StringBuilder();
			int length = rand.nextInt(10) + 5;
			char start = (char) ('A' + rand.nextInt(26));
			wn_sb.append(start);
			for (int j = 0; j < length; j++)
			{
				char c = (char) ('a' + rand.nextInt(26));
				wn_sb.append(c);
			}
			final Language l = l_list.get(rand.nextInt(l_list.size()));
			final Word w = Word.createWord(wn_sb.toString(), l);
			w_list.add(w);
		}

		final Map<Language, List<Word>> w_map = new HashMap<>();
		for (final Language l : l_list)
		{
			w_map.put(l, l.getWords());
		}

		final List<Word> wu_list = new ArrayList<>(w_list);
		while (!wu_list.isEmpty())
		{
			final Word w1 = wu_list.remove(rand.nextInt(wu_list.size()));
			final Word w2 = w_list.get(rand.nextInt(w_list.size()));
			Translation.createTranslation(w1, w2);
		}

		for (int i = 0; i < 50; i++)
		{
			final Language l_from = l_list.get(rand.nextInt(l_list.size()));

			final List<Language> lt_list = new ArrayList<>(l_list);
			lt_list.remove(l_from);
			final Language l_to = lt_list.get(rand.nextInt(lt_list.size()));

			final Calendar cal = Calendar.getInstance();
			final int year = rand.nextInt(2) + Calendar.getInstance().get(Calendar.YEAR);
			final int month = rand.nextInt(12);
			final int date = rand.nextInt(28) + 1;
			final int hourOfDay = rand.nextInt(24);
			final int minute = rand.nextInt(60);
			final int second = rand.nextInt(60);
			cal.set(year, month, date, hourOfDay, minute, second);
			final Date d = cal.getTime();

			final Trial t = Trial.createTrial(d, l_from, l_to);

			final int wca = rand.nextInt(80) + 20;
			final List<Word> wlfrom_list = l_from.getWords().stream().filter(w -> !w.getTranslations(l_to).isEmpty())
					.collect(Collectors.toList());
			for (int j = 0; j < wca && !wlfrom_list.isEmpty(); j++)
			{
				final Word w = wlfrom_list.remove(rand.nextInt(wlfrom_list.size()));
				boolean correct;
				String as;
				if (rand.nextBoolean())
				{
					correct = true;
					as = new WordView(w, l_to).getTranslationsString();
				}
				else
				{
					correct = false;
					final StringBuilder as_sb = new StringBuilder();
					final int asa = rand.nextInt(10) + 5;
					final char st = (char) ('A' + rand.nextInt(26));
					as_sb.append(st);
					for (int k = 0; k < asa; k++)
					{
						final char c = (char) ('a' + rand.nextInt(26));
						as_sb.append(c);
					}
					as = as_sb.toString();
				}
				WordCheck.createWordCheck(w, t, as, correct);
			}
		}
	}

}
