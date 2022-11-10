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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientControllerLogIn extends AbstractNetworkHandler implements Initializable {
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
        if (textFieldUserName.getText().equals("") || passwordFieldPassword.getText().equals("")){
            labelLogInfo.setText("Fill User Name or Password!");
        }
        else {
            Message message = new Message(MessageType.LOGINMESSAGE,
                    textFieldUserName.getText() + "\n" + passwordFieldPassword.getText());
            objectOutputStream.writeObject(message);
            boolean logged = objectInputStream.readBoolean();
            if (logged) {
                labelLogInfo.setText("");
                //TODO switch scenes
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Main-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                String css = this.getClass().getResource("styles.css").toExternalForm();
                scene.getStylesheets().add(css);
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
                stage.getIcons().add(icon);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } else {
                labelLogInfo.setText("Wrong User Name or Password!");
            }

        }
        textFieldUserName.requestFocus();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initSocketAndStreams();
        } catch (IOException e) {
            buttonLogIn.setDisable(true);
            buttonSingUp.setDisable(true);
            closeEverything(socket, objectInputStream, objectOutputStream);
            labelLogInfo.setText("[Error] -Could not connect to server");
            e.printStackTrace();
        }
    }



}