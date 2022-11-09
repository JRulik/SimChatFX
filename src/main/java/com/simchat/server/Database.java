package com.simchat.server;

import com.simchat.shared.dataclasses.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    static HashMap<String, String> hashMapUserLoginAndPassword;
    static HashMap<String, HashMap<String, ArrayList<Message>>> hashMapUserMessagesMap;

    public Database(){
        hashMapUserLoginAndPassword = new HashMap<>();
    }
}
