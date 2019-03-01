package com.visparu.vocabularytrial.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

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
		LogItem.enter();
		ArrayList<Locale> locales = new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.GERMAN));
		LogItem.exit();
		return locales;
	}
	
	public static final Locale getDefaultLocale()
	{
		LogItem.enter();
		final Locale sysDefault = Locale.getDefault();
		Locale l = getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
		LogItem.exit();
		return l;
	}
	
	public static final Locale getLocale()
	{
		LogItem.enter();
		Locale l = I18N.locale.get();
		LogItem.exit();
		return l;
	}
	
	public static final void setLocale(final Locale locale)
	{
		LogItem.enter();
		localeProperty().set(locale);
		Locale.setDefault(locale);
		LogItem.debug("Set internal locale to " + locale.getDisplayName());
		LogItem.exit();
	}
	
	public static final ObjectProperty<Locale> localeProperty()
	{
		LogItem.enter();
		LogItem.exit();
		return I18N.locale;
	}
	
	public static final String get(final String key, final Object... args)
	{
		LogItem.enter();
		final ResourceBundle bundle = ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
		String ret = MessageFormat.format(bundle.getString(key), args);
		LogItem.exit();
		return ret;
	}
	
	public static final ResourceBundle getResources()
	{
		LogItem.enter();
		final ResourceBundle bundle = ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
		LogItem.exit();
		return bundle;
	}
	
	public static final StringBinding createStringBinding(final String key, final Object... args)
	{
		LogItem.enter();
		StringBinding sb = Bindings.createStringBinding(() -> I18N.get(key, args), I18N.locale);
		LogItem.exit();
		return sb;
	}
	
}
