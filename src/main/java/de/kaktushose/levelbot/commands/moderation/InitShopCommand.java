package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.services.SettingsService;
import de.kaktushose.levelbot.listener.DailyRewardListener;
import de.kaktushose.levelbot.listener.ShopListener;
import de.kaktushose.levelbot.util.NumberEmojis;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandController("initshop")
@Permission("moderator")
public class InitShopCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Reaction Shop einrichten",
            usage = "{prefix}initshop <textChannel>",
            desc = "Fügt die benötigten Reactions für den Shop hinzu",
            category = "Moderation"
    )
    public void initShop(CommandEvent event, TextChannel channel) {
        channel.retrieveMessageById(ShopListener.PREMIUM_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(NumberEmojis.ONE))
                .and(msg.addReaction(NumberEmojis.TWO))
                .and(msg.addReaction(NumberEmojis.THREE))
                .queue()
        );
        channel.retrieveMessageById(ShopListener.DJ_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(NumberEmojis.ONE))
                .and(msg.addReaction(NumberEmojis.TWO))
                .and(msg.addReaction(NumberEmojis.THREE))
                .queue()
        );
        channel.retrieveMessageById(ShopListener.NICKNAME_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(NumberEmojis.ONE))
                .and(msg.addReaction(NumberEmojis.TWO))
                .and(msg.addReaction(NumberEmojis.THREE))
                .queue()
        );
        channel.retrieveMessageById(ShopListener.COINS_BOOSTER_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(NumberEmojis.ONE))
                .and(msg.addReaction(NumberEmojis.TWO))
                .queue()
        );
        channel.retrieveMessageById(ShopListener.XP_BOOSTER_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(NumberEmojis.ONE))
                .and(msg.addReaction(NumberEmojis.TWO))
                .queue()
        );
        channel.retrieveMessageById(DailyRewardListener.DAILY_REWARD_MESSAGE_ID).queue(msg -> msg.clearReactions()
                .and(msg.addReaction("\uD83C\uDF81"))
                .queue()
        );
        event.reply(embedCache.getEmbed("shopInitSuccess"));
    }
}
