package com.visparu.vocabularytrial.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class I18N
{

	private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(C11N.getLocale());

	static
	{
		I18N.locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
	}

	public static final List<Locale> getSupportedLocales()
	{
		return new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.GERMAN));
	}

	public static final Locale getDefaultLocale()
	{
		final Locale sysDefault = Locale.getDefault();
		return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
	}

	public static final Locale getLocale()
	{
		return I18N.locale.get();
	}

	public static final void setLocale(final Locale locale)
	{
		localeProperty().set(locale);
		Locale.setDefault(locale);
	}

	public static final ObjectProperty<Locale> localeProperty()
	{
		return I18N.locale;
	}

	public static final String get(final String key, final Object... args)
	{
		final ResourceBundle bundle = ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
		return MessageFormat.format(bundle.getString(key), args);
	}

	public static final ResourceBundle getResources()
	{
		final ResourceBundle bundle = ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
		return bundle;
	}

	public static final StringBinding createStringBinding(final String key, final Object... args)
	{
		return Bindings.createStringBinding(() -> I18N.get(key, args), I18N.locale);
	}



}
