---
layout: page
title: "Web Project"
share: false
image: 
  
  path: images/aes-bg-java.png
  thumbnail: images/aes-bg-java.png
  caption: "AeS header"
---

Java EE 8 Web

[ <i class="fab fa-github"></i> Git Repo](https://github.com/crepeia/aes){: .btn .btn--accent }
[ <i class="fas fa-download"></i> Download](https://github.com/crepeia/aes/archive/gf5-migrating.zip){: .btn .btn--accent }
[ <i class="fas fa-globe"></i> Live Intervention](https://alcoolesaude.com.br/){: .btn .btn--accent }

---

> **Current development branch** <br/>gf5-migrating



* Table of Contents
{:toc}

---
<!--
#### Contents
- [Get started](#get-started)
- [Java](#java)
- [Glassfish Server](#glassfish-server)
- [MySQL Server](#mysql-server)
- [Git and Github](#git-and-github)
- [NetBeans IDE](#netbeans-ide)
- [Donwload the project](#donwload-the-project)
- [Setup the project with Netbeans](#setup-the-project-with-netbeans)
- [Run](#run)
- [Translation](#translation)
- [Graphics](#graphics)
  - [Images and Vectors](#images-and-vectors)
  - [Video](#video)
  - [Stylesheet](#stylesheet)
- [Seeking help?](#seeking-help)
-->

## Get started

You will need the following materials to study and deploy the project.

1. Netbeans IDE 12.0 - to edit the code.
2. Java JDK 8 u241
3. Glassfish Server 5.0.1
4. MySQL 5.7
5. Git and a Github Account

>  *It’s very important to use the versions stated above, since otherwise the system may not work.*

Installation procedures are described below. It was tested on a Windows 10 x64 machine, but it should also work on Linux or Mac aswell, given the proper installation steps.


---

## Java

- Download and install the [**JDK 1.8.0 u241**](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.htm)

---

## Glassfish Server

- Download [**Glassfish Server 5.0.1 - Full Platform**](https://javaee.github.io/glassfish/download)
- Extract somewhere you’ll remember

---

## MySQL Server

- Download the [**MySQL Server Installer**](https://dev.mysql.com/downloads/windows/installer/8.0.html)
- Make sure you download the Web Installer so you can select different server versions
- During Installation, select:
  - MySQL Server 5.7
  - MySQL Connector/J 5.1.23
  - (OPTIONAL) MySQL Workbench - but it’s useful to manage the database, especially if you are not used to the CMD interface.

  <details>
    *Download page*:
    
    ![Figure 1 - Servers tab](../../images/fig10.png)

    *Installation procedure*:
    ![Figure 1 - Servers tab](../../images/fig11.png)
    ![Figure 1 - Servers tab](../../images/fig12.png)
    ![Figure 1 - Servers tab](../../images/fig13.png)
    ![Figure 1 - Servers tab](../../images/fig14.png)
    ![Figure 1 - Servers tab](../../images/fig15.png)
  </details>

### Setup the database
- Set the Server “default-time-zone” property to “+00:00” (otherwise the Java connector may not work properly)
  <details>
    In MySQL Workbench, this setting is located here:
    ![Figure 1 - Servers tab](../../images/fig8.png)
  </details>


- Create the user and the project's database. To do so you can either use the terminal or MySQL Workbench. Run the following SQL script:
  <details>
    In MySQL Workbench:
    ![Figure 1 - Servers tab](../../images/fig9.png)
  </details>

```
create user aes@localhost identified by "aes1235";
```

```
create database aes;
```

```
grant all privileges on aes.* to aes@localhost;
```

---

## Git and Github

- To download and work with the project we recommend *Git*. With *Git* you will be able to fork the project and distribute the code easily. 
- You can download and install it [**here**](https://git-scm.com/downloads)


### For the git newbies
1. Create an account [here](https://www.github.com/join).
2. On the terminal (or Git Bash), type the following commands with your own information.

```
git config --global user.name "John Doe"
```

```
git config --global user.email johndoe@example.com
```

---

## NetBeans IDE

- Download and install [**Netbeans 12LTS**](https://netbeans.apache.org/download/index.html)
- Make sure to use the JDK you downloaded earlier during the installation
- (for Windows) Make sure to always run Netbeans as Administrator

---

## Donwload the project

To download, just open the terminal (or git Bash) in any folder you want and type:

```
git clone https://github.com/crepeia/aes.git
```

```
cd aes
```

```
git checkout gf5-migrating
```

Done. Now the project folder is downloaded and you are in the current development branch.

---

## Setup the project with Netbeans
Now we need to configure the IDE. So run Netbeans as administrator and follow the steps below.

### Setup Glassfish


#### Step 1
Left-click on services tab.

![Figure 1 - Servers tab](../../images/fig1.png)

#### Step 2
Right-click on server

![Figure 2 - Servers tab](../../images/fig2.png)

#### Step 3
Select “Add Server” and add a new Glassfish server.
Make sure to click "Browse" and select the folder you extracted the server to during setup:

![Figure 3 - Servers tab](../../images/fig3.png)

#### Step 4
Click next and finish the wizard with the default settings.

---
### Setup the database
#### Step 1
Still on “Services” tab, expand “Databases”

#### Step 2
Expand "Drivers" and setup the MySQL (Connector/J driver)

![Figure 4 - Databases tab](../../images/fig4.png)

#### Step 3
Click on "Add" and find the MySQL Connector/J 5.1.23 you installed with MySQL

#### Step 4
Click on “Find” button and select the “com.mysql.jdbc.Driver”

![Figure 6 - Databases tab](../../images/fig5.png)


#### Step 5
Create a new connection with this driver:
- Use the user and password created earlier during the database setup
- Use the following JDBC URL:

```
jdbc:mysql://localhost:3306/aes?zeroDateTimeBehavior=convertToNull
```

![Figure 7 - Databases tab](../../images/fig6.png)

---


## Run

If you have completed the steps above, go to Files -> Open Project and browse to the folder where you downloaded the project. 

![Figure 7 - Databases tab](../../images/fig7.png)

Now execute de project using the *Run* command.

---

## Translation

[TO-DO]

---


## Graphics

### Images and Vectors
[TO-DO]

### Video
[TO-DO]

### Stylesheet
[TO-DO]

---

## Seeking help?

Reach us at henriquepgomide@gmail.com
