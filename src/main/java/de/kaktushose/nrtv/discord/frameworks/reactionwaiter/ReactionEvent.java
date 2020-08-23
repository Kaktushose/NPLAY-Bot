package de.kaktushose.nrtv.discord.frameworks.reactionwaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;

/**
 * Represents a reaction event.
 * This class is similar to {@code GuildMessageReactionAddEvent}, but it provides some extra utilities to work with.
 *
 * @author Kaktushose
 * @version 1.0.0
 * @since 1.0.0
 */

public class ReactionEvent extends GuildMessageReactionAddEvent {

    private final Member executor;
    private final EmoteType emote;

    ReactionEvent(GuildMessageReactionAddEvent event, EmoteType emote) {
        super(event.getJDA(), event.getResponseNumber(), event.getMember(), event.getReaction());
        this.emote = emote;
        this.executor = event.getMember();

    }

    /**
     * Returns the codepoint of the emote the reaction event was triggered with.
     *
     * @return the codepoint of the emote
     */
    public EmoteType getEmote() {
        return emote;
    }

    /**
     * Sends a response in the channel where the reaction event was triggered.
     * Returns the RestAction to e.g. access the queue consumer.
     *
     * @param message the String that will be send
     * @return the RestAction
     */
    public RestAction<Message> respond(String message) {
        return channel.sendMessage(message);
    }

    /**
     * Sends a response as an embed in the channel where the reaction event was triggered.
     * Returns the RestAction to e.g. access the queue consumer.
     *
     * @param embedBuilder the EmbedBuilder that will be send
     * @return the RestAction
     */
    public RestAction<Message> respond(EmbedBuilder embedBuilder) {
        return channel.sendMessage(embedBuilder.build());
    }

    /**
     * Sends a response in the channel where the reaction event was triggered.
     * Returns the RestAction to e.g. access the queue consumer.
     * This method might be useful in combination with the MessageBuilder.
     *
     * @param message the message that will be send
     * @return the RestAction
     */
    public RestAction<Message> respond(Message message) {
        return channel.sendMessage(message);
    }

    /**
     * Sends a direct message to the user who triggered the reaction event.
     *
     * @param message the String that will be send
     */
    public void sendPrivateMessage(String message) {
        executor.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    /**
     * Sends an Embed via direct message to the user who triggered the reaction event.
     *
     * @param embedBuilder the EmbedBuilder that will be send
     */
    public void sendPrivateMessage(EmbedBuilder embedBuilder) {
        executor.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(embedBuilder.build()).queue());
    }

    /**
     * Sends a direct message to the user who triggered the reaction event.
     * This method might be useful in combination with the MessageBuilder.
     *
     * @param message the message that will be send
     */
    public void sendPrivateMessage(Message message) {
        executor.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

}
