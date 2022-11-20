v.0.1
--------------------------------------------------------------------------------------------------------------------------------- 
 SimChatFX 
--------------------------------------------------------------------------------------------------------------------------------- 
Simple multi client - server chat application build on JavaFX with SQL database


![image](https://user-images.githubusercontent.com/57802714/202924901-37bc4314-5806-4a0b-b8dc-35468073bff6.png)  ![image](https://user-images.githubusercontent.com/57802714/202924649-96419238-e972-4843-8a6e-5a30d90c6c68.png)

![image](https://user-images.githubusercontent.com/57802714/202924677-4c0ad309-52a7-4060-b23c-64af64fe1a44.png)  ![image](https://user-images.githubusercontent.com/57802714/202924684-ffb2fd8f-656d-47e7-8cf3-72301fdee138.png)


Prerequisites:
- It´s needed to have some web server, which will comunnicate with client and some database server (RDBMS), where database is stored.
Simpliest way (and how this application was developed and tested):
  - Download XAMPP: https://www.apachefriends.org/download.html
  - Install and run. Then run module "Apache" and module "MySQL" (in this sequence).
  - Project is created in IntelliJ IDEA, with newest version (2022.2.3) I think I had no problem with runing JavaFX without any 
moditification of project/IDE (JavaFX should by bundled), but I´m not sure. There is possibility you need install plugins: 
"JavaFX Runtime for Plugins"
"JavaFX" (should be bundled in IDE)
If JavaFX is not working, here are another useful tutorial to make you working for your IDE:
https://www.youtube.com/watch?v=Ope4icw6bVk&t=1s&ab_channel=BroCode    (for IntelliJ IDEA)
https://www.youtube.com/watch?v=_7OM-cMYWbQ&t=312s&ab_channel=BroCode  (for Eclipse -> propably needed)

Settings of application:
- Settings of server and database is in file "ServerMain.java" and it´s theses:

.

    public final static int PORT = 6612;
    public final static String serverHost="localhost";
    public final static String user ="root";
    public final static String password = "";
    public final static String url = "jdbc:mysql://localhost:3306";
    public final static String nameOfDatabase = "simchatfx_database";
   
    
"serverHost", "user","password" and "url" are set on values which works with default settings of XAMPP modules (Apache runs on 
local PC, MySQL server has set these "user","password" and "url" as default. "PORT" was "randomly" selected, can be changed.)

- In "Server.java" there are commented calling of functions to reset database (clear it) with every new run of "Server.java" 
( //databaseMaster.resetDatabase(); ) and to fill database with some predefined values(/users) ( //databaseMaster.fillTestUsers(); )
For your purpose, you can uncomment them and use it (see "databaseMaster.java" to see which users and passwords are filled in).

To run aplication:
- Application has not been builded to standalone application/installation in this repository yet.
To run application in IDE, firstly check that Apache and MySQL module of XAMPP is runnning. Then run "ServerMain.java" which 
runs server side and initialize database. Then you can run "ClientMain.java", which run GUI for individuals clients.

Application control:
- Then you can create user by clicking on button "Sign Up". After creating user, exit SignUp window and you can login with this user 
by clicking on button "Login". In the main window, you can add friend to chat by clicking on button with pictogram "+". 
User which will be added to your friendlist has to be stored in database (created before). After adding user and exiting
AddFriend window, you can chat to user by selecting him in menu on right side of main window. You can write and send message 
by clicking on button "Send" or clicking on Enter on your keyboard.

Some more info and known bugs which needs to be fixed:
- Password are hashed in database (SHA-512 with salt)
- Info about pending messages from other users is stored locally. So when login again/new run of application, this information is lost (should be sotred in database and then retireved). Info about pending messages is also lost when new friend is added.
- You can´t add yourself to your friendlist.

- //TODO:
  - Encrypt server-client communication and content of messages
  - Replace hotfix for regex for user inputs when logging/signup
  - Replace hotfix info about pending messages with color changing list value (with cellfactory)


