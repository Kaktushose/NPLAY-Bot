package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.services.SettingsService;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@CommandController(value = "ignore", category = "Moderation")
@Permission("moderator")
public class IgnoreChannelCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Textkanal muten",
            usage = "{prefix}ignore <channel>",
            desc = "Mutet einen Textkanal",
            category = "Moderation"
    )
    public void onMutedChannelsAdd(CommandEvent event, TextChannel channel) {
        if (!settingsService.isIgnoredChannel(channel.getIdLong())) {
            settingsService.addIgnoredChannel(channel.getIdLong());
        } else {
            event.reply(embedCache.getEmbed("mutedChannelsAdd").injectValue("channel", channel.getAsMention()));
        }
    }

    @Command(
            value = {"remove", "rm"},
            name = "Textkanal unmuten",
            usage = "{prefix}ignore remove <channel>",
            desc = "Unmutet einen Textkanal",
            category = "Moderation"
    )
    public void onMutedChannelsRemove(CommandEvent event, TextChannel channel) {
        settingsService.removeIgnoredChannel(channel.getIdLong());
        event.reply(embedCache.getEmbed("mutedChannelsRemove").injectValue("channel", channel.getAsMention()));
    }

    @Command(
            value = "list",
            name = "Gemutete Textkanäle",
            usage = "{prefix}ignore list",
            desc = "Zeigt alle Channel, die vom Levelsystem ignoriert werden",
            category = "Moderation"
    )
    public void onMutedChannelsList(CommandEvent event) {
        List<Long> mutedChannels = settingsService.getIgnoredChannels();
        StringBuilder list = new StringBuilder();
        mutedChannels.forEach(channelId -> list.append(String.format("<#%d>", channelId)).append("\n"));
        event.reply(embedCache.getEmbed("mutedChannelsList").injectValue("list", list));
    }
}
