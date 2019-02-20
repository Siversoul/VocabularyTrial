package com.visparu.vocabularytrial.util;

import java.io.IOException;
import java.net.URL;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;

import javafx.beans.binding.StringBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class GUIUtil
{

	public static final Stage createNewStage(final String fxmlName, final VokAbfController vac, final StringBinding title)
	{
		try
		{
			final Stage stage = new Stage();
			final URL url = GUIUtil.class.getResource(String.format("/com/visparu/vocabularytrial/gui/fxml/%s.fxml", fxmlName));
			final FXMLLoader loader = new FXMLLoader(url);
			vac.setStage(stage);
			loader.setController(vac);
			loader.setResources(I18N.getResources());
			final Parent root = loader.load();
			final Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.titleProperty().bind(title);
			stage.show();
			return stage;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
