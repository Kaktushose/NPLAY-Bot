package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.service.UserService;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class NitroBoosterListener extends ListenerAdapter {

    private final Levelbot levelbot;

    public NitroBoosterListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {
        UserService userService = levelbot.getUserService();
        event.getGuild().getBoosters().forEach(member -> {
            long userId = member.getIdLong();
            if (userService.isNitroBooster(userId)) {
                userService.changeNitroBoosterStatus(userId, true);
                userService.addMonthlyReward(userId);
                levelbot.getBotChannel().sendMessage("monthly boost").queue();
            } else {
                userService.createNewNitroBooster(userId);
                userService.addOneTimeReward(userId);
                levelbot.getBotChannel().sendMessage("first time boost").queue();
            }
        });
    }
}
