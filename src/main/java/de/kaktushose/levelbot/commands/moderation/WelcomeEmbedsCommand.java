package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

@CommandController("embeds")
@Permission("moderator")
public class WelcomeEmbedsCommand {

    private static final long WELCOME_CHANNEL_ID = 545967082253189121L;
    private final EmbedCache embedCache;

    public WelcomeEmbedsCommand() {
        embedCache = new EmbedCache("welcomeEmbeds.json");
    }

    @Command(
            value = "init",
            name = "Willkommen Embeds senden",
            usage = "{prefix}embeds send",
            desc = "Sendet die Embeds in <#551483788337872927>",
            category = "Moderation"
    )
    public void sendEmbeds(CommandEvent event) {
        for (int i = 0; i < 11; i++) {
            event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID).sendMessage(
                    new EmbedBuilder().setTitle(String.valueOf(i)).build()
            ).queue(message -> message.editMessage(new EmbedBuilder().setTitle(message.getId()).build()).queue());
        }
    }

    @Command(
            value = "resend",
            name = "Willkommen Embeds senden",
            usage = "{prefix}embeds resend <messageId>",
            desc = "Sendet die Embeds in <#551483788337872927>",
            category = "Moderation"
    )
    public void reloadEmbeds(CommandEvent event, long messageId) {
        embedCache.loadEmbedsToCache();
        TextChannel channel = event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID);
        channel.retrieveMessageById(messageId).flatMap(message ->
            message.editMessage(embedCache.getEmbed(String.valueOf(messageId)).toMessageEmbed())
        ).queue();
    }
}
