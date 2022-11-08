package com.simchat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static com.simchat.server.ServerMain.database;


public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private int threadID;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String clientUsername;

    private boolean logged;
    public ClientHandler(int threadID, Socket socket) {
        logged = false;

        this.threadID=threadID;
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Read line
            this.clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
            String messageFromClient;

            while(socket.isConnected()){
                try{
                    if (!logged){
                        String username = bufferedReader.readLine();
                        String password = bufferedReader.readLine();
                        if (database.userLoginAndPasswordMap.containsKey(username)
                                && database.userLoginAndPasswordMap.get(username).equals(password)) {
                            logged=true;
                            bufferedWriter.write(String.valueOf(logged));
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                        else{
                            bufferedWriter.write(String.valueOf(logged));
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                    }
                    else{

                    }
                }
                catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    e.printStackTrace();
                    break;
                }
            }
    }

    public void closeEverything(Socket socket, BufferedReader bufferReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferReader != null){
                bufferReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }



    /* // pred kontrolou vstupu
    @Override
    public void run() {
        DataOutputStream dataOutputStream= null;
        DataInputStream dataInputStream= null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream  = new DataInputStream(socket.getInputStream());

            String line = dataInputStream.readUTF();
            System.out.println(line);
            while (!line.equals("quit"))
            {
                try
                {
                    line = dataInputStream.readUTF();
                    System.out.println("[CLIENT #"+threadID+"] - write: "+line);
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            System.out.println("[CLIENT #"+threadID+"] - Disconnected");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

}
