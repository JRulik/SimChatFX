package com.simchat.server;

import java.util.HashMap;

public class Database {
    static HashMap<String, String> userLoginAndPasswordMap;

    public Database(){
        userLoginAndPasswordMap= new HashMap<>();
    }
}
