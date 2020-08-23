import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.bot.TestBot;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import de.kaktushose.nrtv.discord.frameworks.level.shop.PremiumRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    private static Bot bot;
    private static long buyTime;

    @BeforeAll
    public static void before() {
        bot = new TestBot();
        try {
            bot.start();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
        buyTime = System.currentTimeMillis();
    }

    @AfterAll
    public static void after() {
        bot.shutdown();
    }

    @Test
    public void premiumUnlimitedTest() {
        Item item = bot.getDatabase().getItemType(3, ItemType.PREMIUM);
        assertEquals(0, item.getDuration());
        for (long i = 0; i < System.currentTimeMillis() ; i += 10000) {
            assertEquals("unbegrenzt", item.getRemainingTimeAsDate(i));
            assertFalse(item.isExpiring(i));
        }
    }

    @Test
    public void itemTest() {
        for (ItemType itemType : ItemType.values()) {
            bot.getDatabase().getAllItemTypes(itemType).forEach(item -> {
                for (long i = buyTime - item.getDuration(); i < buyTime; i+= 100) {
                    long remaining = item.getDuration() - (System.currentTimeMillis() - buyTime);
                    if (TimeUnit.MILLISECONDS.toHours(remaining) < 24) {
                        assertTrue(item.isExpiring(buyTime));
                    }
                }
            });
        }
    }

    public void ItemExpirationTest() {

        Item item = new PremiumRole(1, "test item", "only a test", 1296000000, 1);

        for (long i = 0; i < 108000000; i += 60000) {
            if (item.getRemainingTimeAsLong(i) < 24) {
                assertTrue(item.isExpiring(i));
            } else {
                assertFalse(item.isExpiring(i));
            }
        }

    }

}
