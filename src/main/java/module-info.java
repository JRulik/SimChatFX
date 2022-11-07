module com.simchat.simchatfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.simchat.client to javafx.fxml;
    exports com.simchat.client;
}