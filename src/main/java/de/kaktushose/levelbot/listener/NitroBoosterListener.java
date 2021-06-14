package de.kaktushose.levelbot.listener;

import com.github.kaktushose.jda.commands.api.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.services.BoosterService;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
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
        BoosterService boosterService = levelbot.getBoosterService();
        TextChannel botChannel = levelbot.getBotChannel();
        EmbedCache embedCache = levelbot.getEmbedCache();

        // iterate through all actual nitro boosters
        event.getGuild().findMembers(member -> member.getTimeBoosted() != null).onSuccess(boosterList -> {
            boosterList.forEach(member -> {
                long userId = member.getIdLong();

                // user is already registered as an active booster in db, skip this one
                if (boosterService.isActiveNitroBooster(userId)) {
                    return;
                }

                // user is in db, must be a resumed booster
                if (boosterService.isNitroBooster(userId)) {
                    boosterService.changeNitroBoosterStatus(userId, true);
                    boosterService.addMonthlyReward(userId);
                    userService.addUpItem(userId, 3, levelbot);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostResume")
                                    .injectValue("user", member.getEffectiveName())
                                    .toMessageEmbed()
                            )).queue();
                    return;
                }
                // else, user is not in db, must be a first time booster
                boosterService.createNewNitroBooster(userId);
                boosterService.addOneTimeReward(userId);
                userService.addUpItem(userId, 3, levelbot);
                botChannel.sendMessage(member.getAsMention())
                        .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostStart")
                                .injectValue("user", member.getEffectiveName())
                                .toMessageEmbed()
                        )).queue();
            });

            // iterate through all active boosters and compare with actual boosters
            boosterService.getActiveNitroBoosters().forEach(nitroBooster -> {
                Long userId = nitroBooster.getUserId();
                Member member = event.getGuild().getMemberById(userId);

                if (boosterList.stream().map(ISnowflake::getIdLong).noneMatch(userId::equals)) {
                    boosterService.changeNitroBoosterStatus(userId, false);
                    userService.removeItem(userId, 3, levelbot);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostStop")
                                    .injectValue("user", member.getAsMention())
                                    .toMessageEmbed()
                            )).queue();
                }
            });
        });
    }
}
