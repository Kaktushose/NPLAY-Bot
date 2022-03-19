[![Java CI](https://github.com/Kaktushose/Levelbot/actions/workflows/maven.yml/badge.svg)](https://github.com/Kaktushose/Levelbot/actions/workflows/maven.yml)
[![Deploy](https://github.com/Kaktushose/Levelbot/actions/workflows/deploy.yml/badge.svg)](https://github.com/Kaktushose/Levelbot/actions/workflows/deploy.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6eaa0127de99428795b4f5f759da188a)](https://www.codacy.com/gh/Kaktushose/Levelbot/dashboard?utm_source=github.com&utm_medium=referral&utm_content=Kaktushose/Levelbot&utm_campaign=Badge_Grade)
![Generic badge](https://img.shields.io/badge/Version-2.3.0-86c240".svg)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
<a href="https://discord.gg/qcpeZQhJf5">
<img src="https://discordapp.com/api/guilds/367353132772098048/embed.png" alt="discord">
</a>

<img align="right" src="https://cdn.discordapp.com/avatars/497086677853143060/975f993852d45d0f0d5d6ca642186bed.webp?size=256" height=200 width=200>

# Levelbot (NEW Level-System)

This bot was created specifically for the Discord [server](https://discord.gg/qcpeZQhJf5) of the german YouTuber and Twitch Streamer [nordrheintvplay](https://www.youtube.com/user/nordrheintvplay). The core feature of this bot is a leveling system, similar to Mee6, but with a high level of customization. Besides 3 currencies that can be collected, there are 13 items that all bring different functions and advantages. In addition, there are various temporary events, daily rewards and many other useful features.

## Test Server

The bot is in constant development. Join the test [server](https://discord.gg/JYWezvQ) to receive regular updates, make suggestions and test preview versions. This is also the place to get support if you want to host the bot by yourself.

## Installation

Due to the high level of customization, I do not provide a public instance that anyone can invite. However, you can still host your own version of the bot. Therefore, you should have a basic understanding of Maven, MySQL, Discord bots in general and intermediate knowledge of Java and Spring Boot.

### 0. Prerequisites

Make sure to have the following things up and running:

- Java 11
- MySQL Server
- Maven
- git

### 1. Cloning the repo

```
git clone https://github.com/Kaktushose/Levelbot.git
```

### 2. Configuration

Go to the resources folder and add the following `application.properties` file:

```
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbcurl
spring.datasource.username=username
spring.datasource.password=password
```

Make sure to set `ddl-auto` to `none` once you are in production environment.

There are also some hardcoded values (e.g. channel ids) inside the codebase, make sure to change them.

Go to [this](https://github.com/Kaktushose/Levelbot/blob/master/src/main/java/de/kaktushose/levelbot/bot/Levelbot.java#L91) line and provide your own bot token.

### 3. Setting up the database

As soon as you start the bot for the first time, Spring Boot will create all the database tables automatically. Afterwards you can start to fill in values, the column names should be self-explanatory. Feel free to hit me up, if you need help with this step. _Kaktushose#4036 is my discord tag_.

### 4. Building the jar

Once you are done with all configuration steps, you can build the jar and run it:

```
mvn clean package
```

```
java -jar Levelbot.jar
```

## Contributing

If you believe that something is missing, and you want to add it yourself, feel free to open a pull request. I recommend opening an issue first to prevent misunderstandings or waste of time because I'm already making your feature. Please try to keep your code quality at least as good as mine and stick to the design concepts of this project.

## Used Technologies

- [Java 11](https://openjdk.java.net/projects/jdk/11/)
- [Maven](https://maven.apache.org/) - Project Management Software
- [JDA](https://github.com/DV8FromTheWorld/JDA) - Discord API Wrapper
- [jda-commands](https://github.com/Kaktushose/jda-commands) - Command Framework
- [MariaDB](https://mariadb.com/) - Database
- [Spring Boot Data JPA](https://spring.io/projects/spring-data-jpa) - Database Access
