package com.simchat.client;

import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.MessageType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import com.simchat.client.ClientMain.*;

import static com.simchat.client.ClientMain.serverHandler;

public class ControllerLogIn implements Initializable {
    @FXML
    private Label labelLogInfo;
    @FXML
    private Button buttonLogIn;
    @FXML
    private Button buttonSingUp;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private PasswordField passwordFieldPassword;
    @FXML
    protected void signUpButtonClick(ActionEvent e) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SignUp-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("SimChatFX - Sign Up");
        String css = this.getClass().getResource("styles.css").toExternalForm();
        Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
        stage.getIcons().add(icon);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.setResizable(false);
        stage.showAndWait();
        //stage.show();
        textFieldUserName.requestFocus();
    }

    @FXML
    protected void loginButtonClick(ActionEvent e) throws IOException {
        if( Pattern.matches(".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*", textFieldUserName.getText())
                || Pattern.matches(".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*", passwordFieldPassword.getText())){
            Alert alert = new Alert(Alert.AlertType.WARNING, "This characters  \",/;'#=<> ?:@~{}+-\" can´t be used in name or password", ButtonType.OK);
            alert.showAndWait();

            textFieldUserName.requestFocus();
            return;
        }
        if (textFieldUserName.getText().equals("") || passwordFieldPassword.getText().equals("")){
            labelLogInfo.setText("Fill User Name or Password!");
        }
        else {

            Message message = new Message(MessageType.LOGINMESSAGE,
                    textFieldUserName.getText() + "\n" + passwordFieldPassword.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
            serverHandler.sendMessage(message);
            synchronized (this) {
                while (!serverHandler.isProcessedRequest()) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            if (serverHandler.isLogged()) {
                labelLogInfo.setText("");
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Main-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                String css = this.getClass().getResource("styles.css").toExternalForm();
                scene.getStylesheets().add(css);
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

                Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
                stage.getIcons().add(icon);

                ControllerUserWindow controller = fxmlLoader.getController();
                controller.setUsername(textFieldUserName.getText());
                serverHandler.setClientUsername(textFieldUserName.getText())
;
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } else {
                labelLogInfo.setText("Wrong User Name or Password!");
            }

        }
        textFieldUserName.requestFocus(); //proc tady tohle
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            serverHandler = new ServerHandler(this);
            Thread thread = new Thread(serverHandler);
            thread.start();
        } catch (IOException e) {
            buttonLogIn.setDisable(true);
            buttonSingUp.setDisable(true);
            serverHandler.closeEverything();
            labelLogInfo.setText("[Error] - Could not connect to server");
            e.printStackTrace();
        }
    }



}