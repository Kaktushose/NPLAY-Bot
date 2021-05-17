package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.services.SettingsService;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandController("initshop")
public class InitShopCommand {

    public static final String PREMIUM = "⭐";
    public static final String DJ = "\uD83C\uDFB5";
    public static final String NICKNAME = "\uD83D\uDC68\uD83C\uDFFD";
    public static final String COIN_BOOSTER = "\uD83D\uDCB0";
    public static final String XP_BOOSTER = "\uD83C\uDF1F";
    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Reaction Shop aktivieren",
            usage = "{prefix}initshop <textChannel> <messageId>",
            desc = "Aktiviert den Shop über Reactions für die angegebene Nachricht",
            category = "Moderation"
    )
    public void initShop(CommandEvent event, TextChannel channel, long messageId) {
        long guildId = event.getGuild().getIdLong();
        settingsService.setShopChannelId(guildId, channel.getIdLong());
        settingsService.setShopMessageId(guildId, messageId);
        channel.retrieveMessageById(messageId).queue(msg -> msg.clearReactions()
                .and(msg.addReaction(PREMIUM))
                .and(msg.addReaction(DJ))
                .and(msg.addReaction(NICKNAME))
                .and(msg.addReaction(COIN_BOOSTER))
                .and(msg.addReaction(XP_BOOSTER))
                .queue()
        );
        event.reply(embedCache.getEmbed("shopInitSuccess"));
    }
}
