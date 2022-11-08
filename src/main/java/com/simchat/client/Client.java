package com.simchat.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static com.simchat.server.ServerMain.PORT;

public class Client {

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    String username;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", PORT);
        Client client = new Client (socket);
        client.login();
        client.listenForMessage();
        client.sendMessage();
    }

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }

    }

    public void login() throws IOException {
        boolean logged =false;
        while(!logged){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your user name: ");
            username = scanner.nextLine();
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println("Enter your user password: ");
            String password = scanner.nextLine();
            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if(bufferedReader.readLine().equals("true")){
                logged= true;
                System.out.println("Logged in");
            }else{
                System.out.println("Wrong user name or password;");
            }
        }
    }

    public void sendMessage(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String msg = scanner.nextLine();
                bufferedWriter.write(username +": "+ msg);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    String messageFromServer = bufferedReader.readLine();
                    System.out.println(messageFromServer);
                }
                catch(IOException e){
                    closeEverything(socket,bufferedReader, bufferedWriter);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferReader, BufferedWriter bufferedWriter){
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
    /*
    public static void main(String[] args) throws IOException {
        Socket socket= null;
        DataOutputStream dataOutputStream= null;
        Scanner scanner = new Scanner(System.in);

        socket = new Socket("localhost",PORT);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("[CLIENT] - Connected!");

        String input="";
        while(!input.equals("quit")){
                System.out.println("Napiste zpravu:");
                input = scanner.nextLine();
                dataOutputStream.writeUTF(input);
                dataOutputStream.flush();
                System.out.println("[CLIENT] - Msg sent: "+ input);
        }

        dataOutputStream.close();
        socket.close();
        System.out.println("[CLIENT] - close!");
    }*/
}
