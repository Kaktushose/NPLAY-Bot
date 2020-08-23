package de.kaktushose.nrtv.discord.frameworks.reactionwaiter;

import java.util.Arrays;
import java.util.List;

/**
 * The ReactionWaiter waits for reactions being added to messages.
 * There are different variants possible. You can observe a specific message, but also all messages in general.
 * Further you can also only accept specific members.
 *
 * @author Kaktushose
 * @version 1.0.0
 * @since 1.0.0
 */

public class ReactionWaiter {

    private List<EmoteType> emotes;
    private long messageId;
    private long memberId;
    private Runnable runnable;

    /**
     * This constructor creates a reaction waiter that will accept the given emotes, all messages and all users
     *
     * @param emotes the emotes that will be listened for
     */
    public ReactionWaiter(EmoteType... emotes) {
        this.emotes = Arrays.asList(emotes);
        this.messageId = 0;
        this.memberId = 0;
    }

    /**
     * This constructor creates a reaction waiter that will accept the given emotes and all users but is limited to a specific message.
     *
     * @param messageId the id of the message that will be monitored
     * @param emotes the emotes that will be listened for
     */
    public ReactionWaiter(long messageId, EmoteType... emotes) {
        this.emotes = Arrays.asList(emotes);
        this.messageId = messageId;
        this.memberId = 0;
    }


    /**
     * This constructor creates a reaction waiter that will accept the given emotes and is limited to a specific message and user.
     *
     * @param userId the user that will be accepted
     * @param messageId the id of the message that will be monitored
     * @param emotes the emotes that will be listened for
     */
    public ReactionWaiter(long userId, long messageId, EmoteType... emotes) {
        this.emotes = Arrays.asList(emotes);
        this.messageId = messageId;
        this.memberId = userId;
    }

    public ReactionWaiter(long userId, long messageId, List<EmoteType> emotes) {
        this.emotes = emotes;
        this.messageId = messageId;
        this.memberId = userId;
    }

    /**
     * This method gets invoked when a GuildMessageReactionAddEvent matches the specifications given by the used constructor.
     *
     * @param runnable the {@link java.lang.Runnable} containing the {@link ReactionEvent}
     */
    public void onEvent(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Activates the reaction waiter.
     */
    public void startWaiting() {
        ReactionListener.addReactionWaiter(this);
    }

    /**
     * Deactivates the reaction waiter.
     */
    public void stopWaiting() {
        ReactionListener.removeReactionWaiter(this);
    }

    public List<EmoteType> getEmotes() {
        return emotes;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getMemberId() {
        return memberId;
    }

    void called(ReactionEvent event) {
        runnable.run(event);
    }

}
