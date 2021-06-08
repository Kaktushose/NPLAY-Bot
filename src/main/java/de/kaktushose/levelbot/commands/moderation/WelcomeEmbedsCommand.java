package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

@CommandController("embeds")
@Permission("moderator")
public class WelcomeEmbedsCommand {

    public static final long WELCOME_CHANNEL_ID = 851434963316375602L;
    private final EmbedCache welcomeEmbedCache;
    @Inject
    private EmbedCache embedCache;

    public WelcomeEmbedsCommand() {
        welcomeEmbedCache = new EmbedCache("welcomeEmbeds.json");
    }

    @Command(
            value = "init",
            name = "Willkommen Embeds senden",
            usage = "{prefix}embeds send",
            desc = "Sendet die Embeds in <#551483788337872927>",
            category = "Moderation"
    )
    public void sendEmbeds(CommandEvent event) {
        for (int i = 0; i < 12; i++) {
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
        welcomeEmbedCache.loadEmbedsToCache();
        TextChannel channel = event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID);
        channel.retrieveMessageById(messageId).flatMap(message ->
                message.editMessage(welcomeEmbedCache.getEmbed(String.valueOf(messageId)).toMessageEmbed())
        ).queue(success -> {
            event.reply(embedCache.getEmbed("messageReloadSuccess"));
        }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> {
            event.reply(embedCache.getEmbed("messageReloadError"));
        }));
    }
}

