package de.kaktushose.nrtv.discord.frameworks.reactionwaiter;

/**
 * The functional interface used in the onEvent method of {@link de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter}.
 *
 * @author Kaktushose
 * @version 1.0.0
 * @since 1.0.0
 */

public interface Runnable {

    void run(ReactionEvent event);

}
