package exceptions;

import javafx.scene.control.Alert;

public class Alerts {
    public static void infoAlert(String title, String message, String... headerText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        if (headerText.length > 0)
            alert.setHeaderText(headerText[0]);
        else
            alert.setHeaderText("");
        alert.setContentText(message);

        alert.showAndWait();
    }
}
