package de.kaktushose.nrtv.discord.core.bot;

import de.kaktushose.nrtv.discord.core.bot.commands.moderation.*;
import de.kaktushose.nrtv.discord.core.bot.commands.owner.EvalCommand;
import de.kaktushose.nrtv.discord.core.bot.commands.owner.InitCommand;
import de.kaktushose.nrtv.discord.core.bot.commands.user.*;
import de.kaktushose.nrtv.discord.core.bot.listeners.JoinLeaveListener;
import de.kaktushose.nrtv.discord.core.bot.listeners.LevelListener;
import de.kaktushose.nrtv.discord.core.config.BotConfigType;
import de.kaktushose.nrtv.discord.core.config.ConfigFile;
import de.kaktushose.nrtv.discord.frameworks.command.CommandHandler;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionListener;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class LevelBot extends de.kaktushose.nrtv.discord.core.bot.Bot {

    private final CommandHandler commandHandler;

    public LevelBot() {
        super(new ConfigFile("/home/pi/Documents/LevelBot/config.yaml").loadConfig(), BotConfigType.LEVELBOT);

        Logging.getLogger().debug("main bot selected");

        commandHandler = new CommandHandler(this);
        commandHandler.setHelpCommand(new GeneralHelpCommand(this), "hilfe")
                .addCommand(new PingCommand(), "ping")
                .addCommand(new StopCommand(this), "stop")
                .addCommand(new EvalCommand(this), "eval")
                .addCommand(new BulkDeleteCommand(this), "delete")
                .addCommand(new InfoCommand(this), "info", "rank")
                .addCommand(new InitCommand(this), "init")
                .addCommand(new SetCommand(this), "set")
                .addCommand(new AddCommand(this), "add")
                .addCommand(new ShopCommand(this), "kaufen")
                .addCommand(new BotInfoCommand(this), "botinfo")
                .addCommand(new MuteCommand(this), "mute")
                .addCommand(new RemoveCommand(this), "remove")
                .addCommand(new SetPermissionCommand(this), "setperms")
                .addCommand(new ModHelpCommand(this), "modhelp", "modhilfe")
                .addCommand(new ChangeCommand(this), "tauschen")
                .addCommand(new DailyCommand(this), "täglich")
                .addCommand(new LeaderboardCommand(this), "rangliste", "leaderboard", "lb");
    }

    @Override
    public void postStart() {
        getBotChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Bot wurde gestartet!")
                .setDescription(":gear: Version: " + getVersion())
                .build()).queue();
        jda.addEventListener(commandHandler,
                new ReactionListener(),
                new JoinLeaveListener(this),
                new LevelListener(this));
    }
}
