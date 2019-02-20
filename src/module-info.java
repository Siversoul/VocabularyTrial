/**
 * @author z003p1nj
 *
 */
module visparu.vocabularytrial
{	
	requires java.sql;
	requires json.simple;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.base;
	requires transitive javafx.graphics;
	
	opens com.visparu.vocabularytrial.root to javafx.fxml;
	opens com.visparu.vocabularytrial.gui.controllers to javafx.fxml;
	opens com.visparu.vocabularytrial.model.views to javafx.base;
	opens com.visparu.vocabularytrial.model.templates to javafx.base;
	exports com.visparu.vocabularytrial.root;
}
