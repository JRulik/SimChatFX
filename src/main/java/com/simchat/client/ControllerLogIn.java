package com.simchat.client;

import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.simchat.client.ClientMain.serverHandler;
import static com.simchat.client.ClientMain.stageCreator;

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
    protected void signUpButtonClick(ActionEvent e){
        Stage stage = stageCreator.createStage("SignUp-view.fxml","icon.png",
                "styles.css","SimChatFX - Sign Up");
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.showAndWait();
        textFieldUserName.requestFocus();
    }

    @FXML
    protected void loginButtonClick(ActionEvent e) {
        String regexPattern = ".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*";
        if( Pattern.matches(regexPattern, textFieldUserName.getText())
                || Pattern.matches(regexPattern, passwordFieldPassword.getText())){
            Alert alert = new Alert(Alert.AlertType.WARNING, "This characters  \",/;'#=<> ?:@~{}+-\" can´t be used in name or password", ButtonType.OK);
            alert.showAndWait();
            textFieldUserName.requestFocus();
            return;
        }
        if (textFieldUserName.getText().equals("") || passwordFieldPassword.getText().equals("")){
            labelLogInfo.setText("Fill User Name or Password!");
        }
        else {
            Message message = new Message(MessageType.LOGIN_MESSAGE,
                    textFieldUserName.getText() + "\n" + passwordFieldPassword.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
            serverHandler.sendMessage(message);
            synchronized (this) {
                while (!serverHandler.isProcessedRequest()) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Thread interruption error!", ButtonType.OK);
                        alert.showAndWait();
                        serverHandler.closeEverything();
                        System.out.println("[Error] -Thread interruption error!");
                        ex.printStackTrace();
                        System.exit(0);
                    }
                }
            }

            if (serverHandler.isLogged()) {
                labelLogInfo.setText("");
                serverHandler.setClientUsername(textFieldUserName.getText());
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage= stageCreator.createStage(stage,"Main-view.fxml","icon.png","styles.css"
                        ,"SimChatFX - "+ textFieldUserName.getText());
                stage.setOnCloseRequest(event -> {Platform.exit();System.exit(0);});
                stage.show();
            } else {
                labelLogInfo.setText("Wrong User Name or Password!");
            }

        }
        textFieldUserName.requestFocus(); //proc tady tohle
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (serverHandler!=null){
            serverHandler.setGUIThread(this);
        }
        else{
            buttonLogIn.setDisable(true);
            buttonSingUp.setDisable(true);
            labelLogInfo.setText("[Error] - Can´t connect to server!");
        }


    }



}