package de.kaktushose.levelbot.listener;

import com.github.kaktushose.jda.commands.api.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.services.BoosterService;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Long> currentBoosterIds = event.getGuild().getBoosters().stream().map(Member::getIdLong).collect(Collectors.toList());

        event.getGuild().getBoosters().forEach(member -> {
            long userId = member.getIdLong();
            if (boosterService.isNitroBooster(userId)) {
                if (boosterService.getActiveNitroBoosters().stream().noneMatch(nitroBooster -> nitroBooster.getUserId() == userId)) {
                    boosterService.changeNitroBoosterStatus(userId, true);
                    boosterService.addMonthlyReward(userId);
                    botChannel.sendMessage(member.getAsMention()).and(botChannel.sendMessage(
                            embedCache.getEmbed("nitroBoostResume").injectValue("user", member.getAsMention()).toMessageEmbed()
                    )).queue();
                    userService.addUpItem(userId, 3);
                }
            }  else {
                boosterService.createNewNitroBooster(userId);
                boosterService.addOneTimeReward(userId);
                botChannel.sendMessage(member.getAsMention()).and(botChannel.sendMessage(
                        embedCache.getEmbed("nitroBoostStart").injectValue("user", member.getAsMention()).toMessageEmbed()
                )).queue();
            }
        });

        boosterService.getActiveNitroBoosters().forEach(nitroBooster -> {
            User user = event.getJDA().getUserById(nitroBooster.getUserId());
            if (!currentBoosterIds.contains(nitroBooster.getUserId())) {
                boosterService.changeNitroBoosterStatus(nitroBooster.getUserId(), true);
                userService.removeItem(nitroBooster.getUserId(), 3);
                botChannel.sendMessage(user.getAsMention()).and(botChannel.sendMessage(
                        embedCache.getEmbed("nitroBoostResume").injectValue("user", user.getAsMention()).toMessageEmbed()
                )).queue();
            }
        });
    }
}
