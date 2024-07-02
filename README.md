[![Java CI](https://github.com/Kaktushose/NPLAY-Bot/actions/workflows/maven.yml/badge.svg)](https://github.com/Kaktushose/NPLAY-Bot/actions/workflows/maven.yml)
[![Deploy](https://github.com/Kaktushose/NPLAY-Bot/actions/workflows/deploy.yml/badge.svg)](https://github.com/Kaktushose/NPLAY-Bot/actions/workflows/deploy.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6eaa0127de99428795b4f5f759da188a)](https://www.codacy.com/gh/Kaktushose/NPLAY-Bot/dashboard?utm_source=github.com&utm_medium=referral&utm_content=Kaktushose/Levelbot&utm_campaign=Badge_Grade)
![Generic badge](https://img.shields.io/badge/Version-3.0.0-86c240".svg)
[![license-shield](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)]()
<a href="https://discord.gg/qcpeZQhJf5">
<img src="https://discordapp.com/api/guilds/367353132772098048/embed.png" alt="discord">
</a>

<img align="right" src="https://avatars.githubusercontent.com/u/170041565?s=400&u=c3ca1781422297c0f4f4b236e634b4864d431943&v=4" height=200 width=200>

# NPLAY-Bot

This bot was created specifically for the Discord [server](https://discord.gg/qcpeZQhJf5) of the german YouTuber and Twitch Streamer [NPLAY](https://www.youtube.com/nplay). The core feature of this bot is a leveling system, similar to Mee6, but with a high level of customization. 
In addition, there are various temporary events, a karma system, rewards, permissions and many other useful features.

## Test Server

The bot is in constant development. Join the test [server](https://discord.gg/JYWezvQ) to receive regular updates, make suggestions and test preview versions. This is also the place to get support if you want to host the bot by yourself.

## Installation
Due to the high level of customization, I do not provide a public instance that anyone can invite. However, you can still host your own version of the bot. Therefore, you should have a basic understanding of Docker, PostgresSQL and of course Discord bots in general.

> [!IMPORTANT]  
> The bot is designed to only run on a single guild. Multiple guilds are *not* supported. 

### 1. Clone the repo

```
git clone https://github.com/Kaktushose/NPLAY-Bot.git
```

### 2. Initial Configuration

Rename the `.env.example` file to `.env` and provide the given values. 

```env
POSTGRES_DB=database
POSTGRES_USER=user
POSTGRES_PASSWORD=password
POSTGRES_URL=jdbc:postgresql://postgres:5432/database
GF_SECURITY_ADMIN_PASSWORD=password
BOT_GUILD=0123456789
BOT_TOKEN=bot_token
```

### 3. Start the bot

Start the bot by running:

```
docker compose up
```

> [!NOTE]
> The default `docker-compose.yml` will also start up Grafana and Watchtower. These services aren't necessary to run the Bot, so feel free to remove them.

### 4. Further Configuration

To make the bot work properly, you now have to populate the Database correctly. 

1. Update the role ids inside the `item_types` and `ranks` table
2. Update the channel ids inside the `bot_settings` table
3. Add your karma vote emojis (e.g. 👍) inside the `karma_settings` table
4. Use the `/rank-config valid-channels add` command to whitelist  channels for the xp system
5. Manage the permissions with the `/permissions role edit` and `/permissions user edit` commands

That's it. The bot is now fully functional. 

## Credits

I want to thank the following people in now particular order:

1. SimuPlays for creating the concept
2. Combauer for his support with the concept and the logo creation
3. 1Flo3 for testing 
4. MeerBiene for his support and general ideas and for keeping me sane
5. And of course the rest of the NPLAY Discord Team
