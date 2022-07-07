package de.kaktushose.levelbot.leveling.listener;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.UserService;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.events.data.EventService;
import de.kaktushose.levelbot.events.data.collect.CollectEvent;
import de.kaktushose.levelbot.leveling.data.LevelService;
import de.kaktushose.levelbot.leveling.data.rank.Rank;
import de.kaktushose.levelbot.shop.data.ShopService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LevelListener extends ListenerAdapter {

    private final LevelService levelService;
    private final EventService eventService;
    private final UserService userService;
    private final ShopService shopService;
    private final EmbedCache embedCache;
    private final Levelbot levelbot;

    public LevelListener(Levelbot levelbot) {
        this.levelService = levelbot.getLevelService();
        this.eventService = levelbot.getEventService();
        this.userService = levelbot.getUserService();
        this.shopService = levelbot.getShopService();
        this.embedCache = levelbot.getEmbedCache();
        this.levelbot = levelbot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        Guild guild = event.getGuild();
        long guildId = event.getGuild().getIdLong();
        User author = event.getAuthor();
        long userId = author.getIdLong();
        if (author.isBot()) {
            return;
        }
        if (!levelService.isValidMessage(userId, guildId, event.getChannel().getIdLong())) {
            return;
        }
        if (event.getMessage().getContentStripped().length() < 10) {
            return;
        }

        TextChannel channel = levelbot.getBotChannel();

        if (eventService.isCollectEventActive(guildId)) {
            CollectEvent collectEvent = eventService.getActiveCollectEvent(guildId);
            long eventPoints = userService.increaseEventPoints(userId);

            if (eventPoints == collectEvent.getItemBound()) {
                shopService.addItem(userId, collectEvent.getItem().getItemId());
                channel.sendMessage(author.getAsMention())
                        .and(channel.sendMessageEmbeds(embedCache.getEmbed("collectEventItemReward")
                                .injectValue("user", author.getName())
                                .toMessageEmbed())
                        ).queue();

            } else if (eventPoints == collectEvent.getRoleBound()) {
                levelbot.addCollectEventRole(userId);

                channel.sendMessage(author.getAsMention())
                        .and(channel.sendMessageEmbeds(embedCache.getEmbed("collectEventRoleReward")
                                .injectValue("user", author.getName())
                                .toMessageEmbed())
                        ).queue();
            }
        }

        Optional<Rank> optional = levelService.onValidMessage(userId);
        if (optional.isEmpty()) {
            return;
        }

        Rank currentRank = optional.get();
        Rank nextRank = levelService.getNextRank(userId);
        String rewards = levelService.applyRewards(userId, currentRank.getRankId());

        levelbot.addRankRole(userId, currentRank.getRankId());
        levelbot.removeRankRole(userId, levelService.getPreviousRank(userId).getRankId());

        String nextRankInfo = currentRank.equals(nextRank) ? "N/A" : String.format("<@&%d>", nextRank.getRoleId());
        String xp = currentRank.equals(nextRank) ? "0" : String.valueOf(nextRank.getBound());

        channel.sendMessage(author.getAsMention())
                .and(channel.sendMessageEmbeds(embedCache.getEmbed("levelUp")
                        .injectValue("user", author.getAsMention())
                        .injectValue("color", currentRank.getColor())
                        .injectValue("currentRank", guild.getRoleById(currentRank.getRoleId()).getAsMention())
                        .injectValue("nextRank", nextRankInfo)
                        .injectValue("reward", rewards)
                        .injectValue("xp", xp)
                        .toMessageEmbed())
                ).queue();
    }
}
