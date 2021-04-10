package de.kaktushose.levelbot.listener;

import com.github.kaktushose.jda.commands.api.EmbedCache;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.service.LevelService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LevelListener extends ListenerAdapter {

    private final LevelService levelService;
    private final EmbedCache embedCache;

    public LevelListener(LevelService levelService, EmbedCache embedCache) {
        this.levelService = levelService;
        this.embedCache = embedCache;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        if (author.isBot()) {
            return;
        }
        if (!levelService.isValidMessage(author.getIdLong(), guild.getIdLong(), event.getChannel().getIdLong())) {
            return;
        }
        if (event.getMessage().getContentStripped().length() < 10) {
            return;
        }

        Optional<Rank> optional = levelService.addXp(author.getIdLong());
        if (optional.isEmpty()) {
            return;
        }

        Rank currentRank = optional.get();
        Rank nextRank = levelService.getNextRank(author.getIdLong());
        TextChannel channel = guild.getTextChannelById(
                levelService.getGuildSetting(event.getGuild().getIdLong()).getBotChannelId()
        );
        String rewards = levelService.applyRewards(author.getIdLong(), currentRank.getRankId());

        guild.addRoleToMember(author.getIdLong(), guild.getRoleById(currentRank.getRoleId()))
                .and(guild.removeRoleFromMember(author.getIdLong(),
                        guild.getRoleById(levelService.getRank(currentRank.getRankId() - 1).getRoleId()))
                ).queue();

        channel.sendMessage(author.getAsMention())
                .and(channel.sendMessage(embedCache.getEmbed("levelUp")
                        .injectValue("user", author.getAsMention())
                        .injectValue("color", currentRank.getColor())
                        .injectValue("currentRank", guild.getRoleById(currentRank.getRoleId()).getAsMention())
                        .injectValue("nextRank", guild.getRoleById(nextRank.getRoleId()).getAsMention())
                        .injectValue("reward", rewards)
                        .injectValue("xp", nextRank.getBound())
                        .toMessageEmbed())
                ).queue();
    }
}
