import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.bot.TestBot;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.ReactionWaiter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.LoginException;

import static org.junit.jupiter.api.Assertions.*;

public class ReactionTest {

    private static Bot bot;

    @BeforeAll
    public static void before() {
        bot = new TestBot();
        try {
            bot.start();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void after() {
        bot.shutdown();
    }

    @Test
    public void test() {
        long msgId, memberId;
        msgId = 1;
        memberId = 2;
        EmoteType emoteType = EmoteType.BOOSTER;
        ReactionWaiter waiter = new ReactionWaiter(memberId, msgId, emoteType);
        assertEquals(msgId, waiter.getMessageId());
        assertEquals(memberId, waiter.getMemberId());
        assertEquals(emoteType, waiter.getEmotes().get(0));
    }

}
