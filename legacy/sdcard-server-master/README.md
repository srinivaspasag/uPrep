## Documentation for deploying sdcard code.
1. Download the tomcat Using the below link `$ wget http://mirrors.advancedhosters.com/apache/tomcat/tomcat-7/v7.0.61/bin/apache-tomcat-7.0.61.tar.gz`
2. Extract the downloaded file `$ tar -xvzf apache-tomcat-7.0.61.tar.gz` and Copy extracted folder to this location `/usr/local/` using `$ sudo cp -rvf apache-tomcat-7.0.61 /usr/local/tomcat/`
3. Go to `/usr/local` & set the owner permission to that folder & create the soft link for tomcat 
```
$ cd /usr/local/
$ sudo ln -s apache-tomcat-7.0.61 tomcat
$ sudo chown -R your_user_name:group_name (for example sudo chown -R ubuntu:ubuntu tomcat)
```
4. If you don't have jdk setup
    - Download the jdk-6u30 (recommended) from internet & follow the below instructions
```
$ sudo chmod -R +x {jdk bin file name}
$ ./{jdk bin file name}
```
    - Copy extracted folder to this location `/usr/local/` using `$ sudo cp -rvf jdk1.6.0_30 /usr/local/`
    - Create soft link to java using `$ sudo ln -s jdk1.6.0_30 jdk`
    - Set owner permission to java using `$ sudo chown -R ubuntu:ubuntu jdk`
5. Create an environment path from this location `/etc/profile.d`. Use the below commands to create an Environment path for Java & tomcat
    - JAVA environment path
```
$ sudo vi java.sh
$ export JAVA_HOME=/usr/local/jdk
$ export PATH=$JAVA_HOME/bin:$PATH
```
    - Tomcat environment path
```
$ sudo vi tomcat.sh
$ export CATALINA_HOME=/usr/local/tomcat
$ export APP_SERVER_HOME=$CATALINA_HOME
```
6. Use the below command to use java & tomcat
```
$ source /etc/profile.d/tomcat.sh
$ source /etc/profile.d/java.sh
```
7. To Start & stop the tomcat service use the below command in `/usr/local/tomcat/bin/` for starting `$ ./startup.sh` & for stopping `$ ./shutdown.sh`.
8. Verify the tomcat installation in web browser `localhost:8080`
9. Set the user credential to tomcat Application manager goto `/usr/local/tomcat/bin/conf/tomcat-user.xml` and modify the file accordingly using the below snippet
```
<tomcat-users>
<role rolename="manager-gui"/><user username="tomcat" password="tomcat" roles="manager-gui"/>
</tomcat-users>
```
10. Go to user home folder & create a folder with below command `$ mkdir -p ~/data/deskAppData/fs`
11. Go to the root folder or sdcard code and create a file named `build.properties` keep the following lines in that file.
```
REQ_JAVA_HOME=/usr/local/jdk
REQ_APP_SERVER_HOME=/usr/local/tomcat
```
12. Set the organization URL & desktop local file storage location in `conf/remote.properties` file. Modify it accordingly. Here is the snip of that file for production.
```
remote.host.url=https://cmds.learnpedia.com
remote.host.router=extCMDSUploadAppRouter
desktop.app.id=DesktopApp
desktop.app.folder.name=Desktop App
desktop.storage.location=/home/ubuntu/data/deskAppData/fs
```
13. Delete .vedantu-dbs folder in home folder.
14. For deploying the application in tomcat goto root folder of sdcard and use `$ ./deploy.sh`.
15. Go to web browser & type localhost:8080 & click on manager app and give the credentials `User Name: tomcat` & `Password: tomcat`.
16. Click the link `vedantu-ext-cmds-uploader` and enter the URL as per your domain & organization id. For example `https://cmds.learnpedia.in/org/learnpedia` for production. and `https://cmdsqa.learnpedia.in/org/lptest` for QA
17. After that enter your organization Super admin details.
18. To get Secret Key & Authentication ID, goto https://tools.learnpedia.in , click the organization menu and click view on the organization required.
19. Setup with required things and select VIEW/IMPORT LIBRARIES to Download program libraries and flash SD cards. Follow the instructions and download the required sdcard.
20. After download it will show one flash Button click that button & enter the Path to save the file. The path will be the folder in your system which you should create and give the path.
    - After downloading is done. Go to the path location `$PATH/sdcard/card-1`. 
    - Copy vedantu folder to sdcard root. Insert the sdcard to TABLET.
    - Open the LMS Application. Give the correct email and access code for sdcard to sync.
    - The provided access details will work for only one device (First Login device)