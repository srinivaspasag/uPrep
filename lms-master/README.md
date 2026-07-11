## Documentation for setting up LMS (Learning Management System)

**Get the following files for proceeding with the installation** 

1. play-1.2.4.zip
2. elasticsearch-0.20.6.zip
3. jdk-6u30-linux-x64.bin
4. play-2.1.0.zip
5. hbase-0.92.1.zip

**Installation Files - [Download](https://drive.google.com/drive/folders/1wRC2FVcs9HK4dyqA2hHVhUVXNSLNsBYp?usp=sharing)

**Follow the following steps to complete installation of the required softwares for setting up LMS**

* Use below commands to extract the required softwares

```
$ unzip elasticsearch-0.20.6.zip
$ unzip play-1.2.4.zip
$ unzip play-2.1.0.zip
$ unzip hbase-0.92.1.zip
$ sudo chmod -R +x jdk-6u30-linux-x64.bin
$ ./jdk-6u30-linux-x64.bin
```
* Use below commands to copy extracted softwares to `/usr/local`

```
$ sudo cp -rvf elasticsearch-0.20.6 /usr/local/
$ sudo cp -rvf hbase-0.92.1 /usr/local/
$ sudo cp -rvf jdk1.6.0_30 /usr/local/
$ sudo cp -rvf play-2.1.0 /usr/local/
$ sudo cp -rvf play-1.2.4 /usr/local/
```

* Naviagte to cd /usr/local/

* Use below commands to provide ownership to copied folders. Get your user:group using `groups $USER`

```
$ sudo chown -R user:group elasticsearch-0.20.6
$ sudo chown -R user:group hbase-0.92.1
$ sudo chown -R user:group jdk1.6.0_30
$ sudo chown -R user:group play-2.1.0
$ sudo chown -R user:group play-1.2.4
```

* Use below commands to create the short link in this location `/usr/local`

```
$ sudo ln -s elasticsearch-0.20.6 elasticsearch
$ sudo ln -s hbase-0.92.1 hbase
$ sudo ln -s jdk1.6.0_30 jdk
$ sudo ln -s play-1.2.4 play
```
* Use below commands to provide ownership to created softlinks. Get your user:group using `groups $USER`

```
$ sudo chown -R user:group elasticsearch
$ sudo chown -R user:group hbase
$ sudo chown -R user:group jdk
$ sudo chown -R user:group play
```
* Navigate to /usr/local/hbase/bin
* Use below commands to to provide executable permissions for script files in `/usr/local/hbase/bin` for starting HBase.
```
$ chmod +x *.sh
$ chmod +x hbase
```

* Naviagte to /etc/profile.d
* Use below commands to create following files in `/etc/profile.d/` for environment paths of play, java , elasticsearch and fill the files with the given lines.

```
$ sudo java.sh 
  export JAVA_HOME=/usr/local/jdk
  export PATH=$JAVA_HOME/bin:$PATH
$ sudo play.sh
  export PLAY_HOME=/usr/local/play
  export PATH=$PLAY_HOME:$PATH
$ sudo vi elasticsearch.sh
  export ES_HOME=/usr/local/elasticsearch
  export PATH=$ES_HOME/bin:$PATH
```

* For newer systems(If there is a problem when setting environment variable for play,install python)
```
$ sudo apt install python
```

* Create `.zowieAllowedApps` in home of user, contains list of services allowed in this particular server.  Fill the file with below lines.

  > billing/billing-services
  >
  > social/social-services
  >
  > user/user-services
  >
  > cmds/cmds-services
  >
  > viewer/viewer-services
  >
  > organization/organization-services
  >
  > event/event-bus-mgmt
  >
  > ui/cmds-app
  >
  > ui/web-app
  >
  > ui/tools
  >
  > ui/learn-app
  >
  > comm/comm-services
  >
  > board/board-services
  >
  > content/content-services
  >
  > commons

* Create `.zowieAppsPlayPath` in the home of user, which contains path of play framework installation to use for running frontend services. Fill the file with below lines

  > ui/web-app:/usr/local/play-1.2.4
  >
  > ui/tools:/usr/local/play-1.2.4
  >
  > ui/cmds-app:/usr/local/play-1.2.4
  >
  > ui/learn-app:/usr/local/play-1.2.4

* Create `.zowiePlayPath`  in the home of user, which contains path of play framework installation to use for running backend services. Fill the file with following line.

  > /usr/local/play-2.1.0

* Create `.zowieFwkId` in the home of user, which contains mode for running the modules/services. For `localhost` the file will have `local` .

* Create `.zowieClusteredApps` in the home of user, which defines clusters of modules/services that can be managed (start/stop) using the cluster name. 

  > lp: billing/billing-services, user/user-services, organization/organization-services, content/content-services, cmds/cmds-services, board/board-services, event/event-bus-mgmt, social/social-services, comm/comm-services,viewer/viewer-services, ui/cmds-app, ui/web-app, ui/tools, ui/learn-app

* Edit `/usr/local/elasticsearch/config/elasticsearch.yml` file and change the below lines in the file accordingly.

  > path.data: /home/ubuntu/data/es/data
  >
  > path.work: /home/ubuntu/data/es/work
  >
  > path.log: /home/ubuntu/data/es/logs
  >
  > network.host: 127.0.0.1
  
* Edit `/usr/local/hbase/conf/hbase-site.xml` file and add the following configuration in that file
```
<configuration>
<property>
<name>hbase.rootdir</name>
<value>file:///home/username/data/hbase</value>
</property>
<property><name>dfs.support.append</name>
<value>true</value>
</property>
<property>
<name>hbase.zookeeper.quorum</name>
<value>localhost</value>
</property>
<property>
<name>hbase.zookeeper.property.clientPort</name>
<value>2181</value>
</property>
</configuration>
```
* Install remaining required softwares using below commands 

  * **Nginx** to expose lms modules as a web interface to the application services

    `sudo apt-get install nginx`

  * **Git** for version control `sudo apt-get install git`

  * **AVCONVUtil** to convert, compress and generate thumbnail for different video
    formats and converts it to webm format. `sudo apt-get install libav-tools`

  * **qpdf ** & **ghostscript** to process documents `sudo apt-get install qpdf`
    & `sudo apt-get install ghostscript`

  * **mkclean** to clean and optimize Matroska (.mkv / .mka / .mks / .mk3d) and WebM
    (.webm / .weba) files that have already been muxed.

    ```
    $ wget sourceforge.net/projects/matroska/files/mkclean/mkclean-0.8.7.tar.bz2
    $ tar -xvjf mkclean-0.8.7.tar.bz2
    $ sudo apt-get install build-essential checkinstall
    $ ./configure
    $ make -C mkclean
    $ sudo checkinstall –y
    ```

  * Check if libreoffice is installed to process excel files uploaded.

  * To Install MongoDB - [Click here](https://drive.google.com/open?id=1zu6FA9cbrk9-MqOsTHZDKcmPeqnaZFEq) and download the debian package files and install them one by one. If for some reason, the link doesn't work, Contact Learnpedia's Admin.

    ​

* Edit `/etc/hosts` to add the following lines to the file.

  > 172.31.X.X cmds.learnpedia.in cmds
  >
  > 172.31.X.X learn.learnpedia.in learn
  >
  > 172.31.X.X tools.learnpedia.in tools
  >
  > 172.31.X.X imglearn.learnpedia.in imglearn
  >
  > 172.31.X.X imgcmds.learnpedia.in imgcmds
  >
  > 172.31.X.X mongo.learnpedia.in mongo
  >
  > 127.0.0.1 es1.learnpedia.in es1
  >
  > 172.31.X.X ws2.learnpedia.in ws2
  >
  > 172.31.X.X ws1.learnpedia.in ws1
  >
  > 172.31.X.X memc.learnpedia.in memc
  >
  > 172.31.X.X img.ws2.learnpedia.in img.ws2

* Now clone the lms from gitlab in the home of $USER and use `mv ~/lms ~/lms/vedantu/zowie` on the cloned folder in the home path.

* For compiling the cloned code use the below commands or else use the below script

Script File - [Download](https://drive.google.com/file/d/1I4zWU_9_QS84KdaWzRaTND6vUokJNMWV/view?usp=sharing)

  * For compiling backend services

  ```
  $ billing/billing-services /usr/local/play-2.1.0/play compile
  $ social/social-services /usr/local/play-2.1.0/play compile
  $ user/user-services/usr/local/play-2.1.0/play compile
  $ cmds/cmds-services /usr/local/play-2.1.0/play compile
  $ viewer/viewer-services /usr/local/play-2.1.0/play compile
  $ organization/organization-services /usr/local/play-2.1.0/play compile
  $ event/event-bus-mgmt /usr/local/play-2.1.0/play compile
  $ comm/comm-services /usr/local/play-2.1.0/play compile
  $ board/board-services /usr/local/play-2.1.0/play compile
  $ content/content-services /usr/local/play-2.1.0/play compile
  $ commons /usr/local/play-2.1.0/play compile
  ```

  * For resolving dependencies in frontend

  ```
  $ /usr/local/play-1.2.4/play deps ~/lms/vedantu/zowie/ui/ui-commons
  $ /usr/local/play-1.2.4/play deps ~/lms/vedantu/zowie/ui/cmds-app
  $ /usr/local/play-1.2.4/play deps ~/lms/vedantu/zowie/ui/web-app
  $ /usr/local/play-1.2.4/play deps ~/lms/vedantu/zowie/ui/tools
  $ /usr/local/play-1.2.4/play deps ~/lms/vedantu/zowie/ui/learn-app
  ```

* Commands to be used **vPlay.sh**  file
    * start <service>  for starting the applications
    * stop <service>   for stopping the applications
    * status <service> for show the status applications, whether they are in stopped or running condition
    * clean <service>  for clean the compiled target directories
    * deps <service>   for resolve the dependencies of the play application

* Every service has conf directory, which has configurations defined in various configuration files based on fwkId (frameworkid).Frameworkid specifies the mode of deployment as local, dev, qa, prod etc.

* **vPlay.sh** uses fwkId described in **.zowieFwkId** for finding appropriate configuration and start services configured with correct configuration file. By default play framework creates application.conf which should hold mode agnostic configurations.

* Use the below commands to Start & stop ALL services in single command in `~/lms/vedantu/zowie`

  ```
  $ ./vPlay.sh
  $ show id:lp
  $ start id:lp
  $ stop id:lp
  ```

  * To Start the single service use the below mentioned commands

    ```
    ./vPlay.sh$ start social/social-services
    ./vPlay.sh$ start user/user-services
    ./vPlay.sh$ start cmds/cmds-services
    ./vPlay.sh$ start viewer/viewer-services
    ./vPlay.sh$ start organization/organization-services
    ./vPlay.sh$ start event/event-bus-mgmt
    ./vPlay.sh$ start comm/comm-services
    ./vPlay.sh$ start board/board-services
    ./vPlay.sh$ start content/content-services
    ./vPlay.sh$ start ui/cmds-app
    ./vPlay.sh$ start ui/web-app
    ./vPlay.sh$ start ui/tools
    ./vPlay.sh$ start ui/learn-app
    ```

  * To Stop the single service use the below mentioned commands

    ```
    ./vPlay.sh$ stop social/social-services
    ./vPlay.sh$ stop user/user-services
    ./vPlay.sh$ stop cmds/cmds-services
    ./vPlay.sh$ stop viewer/viewer-services
    ./vPlay.sh$ stop organization/organization-services
    ./vPlay.sh$ stop event/event-bus-mgmt
    ./vPlay.sh$ stop comm/comm-services
    ./vPlay.sh$ stop board/board-services
    ./vPlay.sh$ stop content/content-services
    ./vPlay.sh$ stop ui/cmds-app
    ./vPlay.sh$ stop ui/web-app
    ./vPlay.sh$ stop ui/tools
    ./vPlay.sh$ stop ui/learn-app
    ```

* After starting all the services use the below commands to setup elasticsearch database
  For the setting up of ES (ElasticSearch) mappings refer scripts under `~lms/vedantu/zowie/environment/scripts/prod/elasticsearch`

  > es_entity_mapping.sh : To Create indexes & mapping relationships in ES.
  
  > es_graph_mapping.sh : To Adds basic relationships in ES.

* Use the below commands to Start the elastic database setup from the path `~/lms/vedantu/zowie`

  ```
  $ ./environment/scripts/prod/elasticsearch/es_entity_mapping.sh es1.learnpedia.in
  $ ./environment/scripts/prod/elasticsearch/es_graph_mapping.sh es1.learnpedia.in
  ```

* Run `~/lms/vedantu/zowie/event/event-bus-mgmt/scripts/create_hbase.rb` from `/usr/local/hbase/bin` using the command `./hbase shell ~/lms/vedantu/zowie/event/event-bus-mgmt/scripts/create_hbase.rb` to create tables in hbase db.

* To start and stop hbase use `./start-hbase.sh` and `./stop-hbase.sh` in the path `/usr/local/hbase/bin`

* Using **Event Bus Management Console** goto `~/lms/vedantu/zowie/event/event-bus-mgmt/scripts` and run `./console.sh localhost 19015` and use **start** , **stop** & **status** commands to start, stop and get the status of the events respectively.

## After Setting up is done

Start all events in event bus console and hbase.

Temp location is hard coded to /home/vedantu/data change it accordingly in **cmds-services/conf** and **commons/conf**.

Goto http://localhost:19003/ and use **username** : **local@localhost.loc** and **password** : **password** to login and proceed to create a local organization. 



Contents
--------
1. vPlay properties files
2. Framework-ids
3. .gitignore
4. Global.java
5. Service ports
6. conf file inclusions




1. vPlay properties files
=========================
Following files should be created in the $HOME of the user who would be running the projects:
- .<BASEDIRNAME>FwkId
- .<BASEDIRNAME>PlayPath
- .<BASEDIRNAME>AllowedApps
- .<BASEDIRNAME>AppsPlayPath
- .<BASEDIRNAME>ClusteredApps

Note the "." with which each of the above file names start.
Also note that <BASEDIRNAME> specifies the name of the directory in which vPlay.sh is present.
For example if <BASEDIRNAME> is "zowie" then the files would be ".zowieFwkId", ".zowiePlayPath" etc.

Lets see what each of these files is used for.


1.a. .<BASEDIRNAME>FwkId
------------------------
This file contains the default framework-id to be used in case it is not specified explicitly in .<BASEDIRNAME>AllowedApps for any project. For example,

test


1.b. .<BASEDIRNAME>PlayPath
---------------------------
This file contains the path of the base directory of the default play version to be used in case it is not specified explicitly in .<BASEDIRNAME>AppsPlayPath for any project. For example,

/usr/local/play-2.1.0


1.c. .<BASEDIRNAME>AllowedApps
------------------------------
This file contains the list of projects that are allowed to be run on this system. Each line contains 1 allowed project. If you wish to use some project specific framework-id you can specify that as a colon (:) separated suffix of the project. For example,

web-app
cmds:dev

In the above example the "web-app" prject would be started using the default framework-id specified in .<BASEDIRNAME>FwkId while the "cmds" project would be started using "dev" framework-id.


1.d. .<BASEDIRNAME>AppsPlayPath
-------------------------------
This file contains the list of projects for which you wish to use a play version which is different from what is specified in .<BASEDIRNAME>PlayPath. For example,

web-app:/usr/local/play-1.2.4


1.e. .<BASEDIRNAME>ClusteredApps
--------------------------------
This file contains a named-groups (id=project1, project2,...) of projects which you would like to start, stop or show using the "id:<id-name>" parameter in vPlay.sh. For example,

localcmds=cmds,user,organization,event-bus-processor
localtools=tools,user

In the above example there are 2 ids - "localcmds" and "localtools" that denote the group of projects you would like to refer together in vPlay.sh while performing various operations.




2. Framework-ids
================
local : Qualifies properties to be used in local development environment
dev : Qualifies staging properties
prod : Qualifies production mode properties
demo : Qualifies demo mode properties

Note: Starting with play-2.x we would keep these property files in the "conf" folder and include the application.conf in it.
Note: The corresponding name of the property file would be <framework-id>.conf.




3. .gitignore
=============
You must copy .gitignore in any new project that you create.




4. Global.java
==============
Every service source [<project>-services] created by you must contain Global.java at <your_service>/app level. This Global class should extend  com.vedantu.VedantuGlobalSettings.




5. Service ports
================
19001 : web-app
19002 : cmds-app
19003 : tools

19011 : user
19012 : organization
19013 : content
19014 : cmds
19015 : event-bus
19016 : board
19017 : comm 
19018 : social
19019 : viewer



6. conf file inclusions
=======================
"application.conf" file is found in <project>/conf/ path. This is included in "commons/conf/commons.conf", which is then included in "commons/conf/commons.local.conf" (for "local" framework-id, and likewise for others). You need to specify a "local.conf" file in the "conf" folder of your project. This should include "commons.local.conf".

In general, in order to run the project in a specific framework-id (fwkId), you need to include a file "<fwkId>.conf" in the "conf" folder of your project. This file should include "commons.<fwkId>.conf" file.

In a nutshell this is how the conf files would be included:
application.conf  --- (included in) --->  commons.conf  --- (included in) --->  commons.<fwkId>.conf  --- (included in) --->  <fwkId>.conf

