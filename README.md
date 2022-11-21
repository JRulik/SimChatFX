v.0.1
--------------------------------------------------------------------------------------------------------------------------------- 
 SimChatFX 
--------------------------------------------------------------------------------------------------------------------------------- 
Simple multi client - server chat application build with JavaFX and using SQL database. 

![image](https://user-images.githubusercontent.com/57802714/202924901-37bc4314-5806-4a0b-b8dc-35468073bff6.png)  ![image](https://user-images.githubusercontent.com/57802714/202924649-96419238-e972-4843-8a6e-5a30d90c6c68.png)

![image](https://user-images.githubusercontent.com/57802714/203175934-0be087e0-9f16-4bfa-bc40-54911df3fcf4.png)


Prerequisites:
- It´s needed to have some web server, which will communicate  with client and some database server (RDBMS), where database is stored.
Simplest way (and how this application was developed and tested):
  - Download XAMPP: https://www.apachefriends.org/download.html
  - Install and run. Then run module "Apache" and module "MySQL" (in this sequence).
 
- Project is created in IntelliJ IDEA, with the newest version (2022.2.3) I think I had no problem with running JavaFX without any 
modification of project/IDE (JavaFX should by bundled), but I´m not sure. There is possibility you need install plugins: "JavaFX Runtime for Plugins" and "JavaFX" (should be bundled in IDE)

- If JavaFX is not working, here are another useful tutorial to make you working for your IDE:
https://www.youtube.com/watch?v=Ope4icw6bVk&t=1s&ab_channel=BroCode    (for IntelliJ IDEA)
https://www.youtube.com/watch?v=_7OM-cMYWbQ&t=312s&ab_channel=BroCode  (for Eclipse -> probably needed)

 - In ItelliJ IDEA is needed to add "mysl-connector-j-8.0.31.jar" from lib folder of this project to module path (right click on project -> Open Module Settings -> click on "+" pictogram -> add "mysl-connector-j-8.0.31.jar" from lib folder)

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

To run application:
- Application has not been built to standalone application/installation in this repository yet.
To run application in IDE, firstly check that Apache and MySQL module of XAMPP is running. Then run "ServerMain.java" which 
runs server side and initialize database. Then you can run "ClientMain.java", which run GUI for individuals clients. You can run more clients at once and
chat between each other.

Application control:
- Then you can create user by clicking on button "Sign Up". After creating user, exit SignUp window and you can login with this user 
by clicking on button "Login". In the main window, you can add friend to chat by clicking on button with pictogram "+". 
User which will be added to your friendlist has to be stored in database (created before). After adding user and exiting
AddFriend window, you can chat to user by selecting him in menu on right side of main window. You can write and send message 
by clicking on button "Send" or clicking on Enter on your keyboard.

Database:
- Database is called simchatx_database.
- Users and their passwords are stored in "user" table
- Users friendlist and messages is stored in table "nameofuser_friendlist" and "nameofuser_messages" (so for every user there is row in "users" table and 2 additional
tables

Some more info and known bugs which needs to be fixed:
- There is no option to delete messages, users, or friend from friendlist etc.
- Password are hashed in database (SHA-512 with salt)
- Info about pending messages from other users is stored locally. So when login again/new run of application, this information is lost (should be stored in database and then retrieved). Info about pending messages is also lost when new friend is added (GUI is refreshed)
- You can´t add yourself to your friendlist.

- //TODO:
  - Make robustnes user input, e.g. depending on the database parameters (e.g. condition on length of username and password)
  - Sorting of friends in GUI
  - Encrypt server-client communication and content of messages
  - Replace hotfix for regex for user inputs when logging/signup
  - Replace hotfix info about pending messages with color changing list value (with cellfactory)


