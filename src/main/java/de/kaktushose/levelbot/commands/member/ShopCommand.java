package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.database.service.LevelService;
import de.kaktushose.levelbot.database.service.UserService;
import de.kaktushose.levelbot.util.NumberEmojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@CommandController({"kaufen", "shop"})
public class ShopCommand {

    public static final String BACK = "◀️";
    public static final String CANCEL = "❌";
    public static final String PREMIUM = "⭐";
    public static final String DJ = "\uD83C\uDFB5";
    public static final String NICKNAME = "\uD83D\uDC68\uD83C\uDFFD";
    public static final String COIN_BOOSTER = "\uD83D\uDCB0";
    public static final String XP_BOOSTER = "\uD83C\uDF1F";

    @Inject
    private UserService userService;
    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;

    private EmbedBuilder shopOverview;
    private Map<ItemCategory, EmbedBuilder> specificShops;

    public ShopCommand() {
        specificShops = new HashMap<>();
    }

    @Command(
            name = "Level-Shop",
            usage = "{prefix}kaufen",
            desc = "Mit diesem Command kannst du Items aus dem Levelshop kaufen",
            category = "Levelsystem"
    )
    public void onShop(CommandEvent event) {
        generateEmbeds();
        sendDefaultShop(event, null);
    }

    private void sendDefaultShop(CommandEvent event, Message shopMessage) {
        Consumer<Message> success = message -> {

            message.clearReactions()
                    .and(message.addReaction(PREMIUM))
                    .and(message.addReaction(DJ))
                    .and(message.addReaction(NICKNAME))
                    .and(message.addReaction(COIN_BOOSTER))
                    .and(message.addReaction(XP_BOOSTER))
                    .and(message.addReaction(CANCEL))
                    .queue();

            ReactionWaiter reactionWaiter = new ReactionWaiter(message, event.getMember(), PREMIUM, DJ, NICKNAME, COIN_BOOSTER, XP_BOOSTER, CANCEL);
            reactionWaiter.onEvent(reactionEvent -> {
                switch (reactionEvent.getEmote()) {
                    case PREMIUM:
                        sendSpecificShop(event, message, ItemCategory.PREMIUM);
                        break;
                    case DJ:
                        sendSpecificShop(event, message, ItemCategory.DJ);
                        break;
                    case NICKNAME:
                        sendSpecificShop(event, message, ItemCategory.NICKNAME);
                        break;
                    case COIN_BOOSTER:
                        sendSpecificShop(event, message, ItemCategory.COINS_BOOSTER);
                        break;
                    case XP_BOOSTER:
                        sendSpecificShop(event, message, ItemCategory.XP_BOOSTER);
                        break;
                    case CANCEL:
                        message.delete().and(event.getMessage().delete()).queue();
                        break;
                }
                reactionWaiter.stopWaiting(false);
            });
        };

        if (shopMessage == null) {
            event.reply(shopOverview, success);
        } else {
            shopMessage.editMessage(shopOverview.build()).queue(success);
        }
    }

    private void sendSpecificShop(CommandEvent event, Message shopMessage, ItemCategory itemCategory) {
        EmbedBuilder embedBuilder = specificShops.get(itemCategory);
        List<Item> items = levelService.getItemsByCategoryId(itemCategory.id);
        long amount = items.stream().filter(Item::isVisible).count();
        Consumer<Message> success = message -> {
            message.clearReactions().queue();
            for (int i = 0; i < amount; i++) {
                message.addReaction(EmoteType.getNumber(i + 1).unicode).queue();
            }
            message.addReaction(BACK)
                    .and(message.addReaction(CANCEL))
                    .queue();

            ReactionWaiter reactionWaiter = new ReactionWaiter(
                    message,
                    event.getMember(),
                    BACK, CANCEL,
                    NumberEmojis.ONE, NumberEmojis.TWO, NumberEmojis.THREE, NumberEmojis.FOUR
            );
            reactionWaiter.onEvent(reactionEvent -> {
                int variant = 0;
                switch (reactionEvent.getEmote()) {
                    case NumberEmojis.ONE:
                        variant = 1;
                        break;
                    case NumberEmojis.TWO:
                        variant = 2;
                        break;
                    case NumberEmojis.THREE:
                        variant = 3;
                        break;
                    case NumberEmojis.FOUR:
                        variant = 4;
                        break;
                    case BACK:
                        sendDefaultShop(event, message);
                        reactionWaiter.stopWaiting(false);
                        return;
                    case CANCEL:
                        message.delete().and(event.getMessage().delete()).queue();
                        reactionWaiter.stopWaiting(false);
                        return;
                }
                if (amount < variant) {
                    return;
                }

                Item item = items.get(variant - 1);
                Optional<String> buyResult = buy(event.getMember(), item);

                if (buyResult.isEmpty()) {
                    message.editMessage(embedCache.getEmbed("shopSuccess")
                            .injectValue("item", item.getName())
                            .injectValue("days", TimeUnit.MILLISECONDS.toDays(item.getDuration()))
                            .toMessageEmbed()
                    ).and(message.clearReactions()).queue();
                } else {
                    message.editMessage(embedCache.getEmbed("shopError")
                            .injectValue("message", buyResult.get())
                            .toMessageEmbed()
                    ).queue(errorMessage -> {
                        errorMessage.clearReactions().queue();
                        errorMessage.addReaction(BACK).and(errorMessage.addReaction(CANCEL)).queue();
                        ReactionWaiter waiter = new ReactionWaiter(message, event.getMember(), BACK, CANCEL);
                        waiter.onEvent(errorMessageEvent -> {
                            if (errorMessageEvent.getEmote().equals(BACK)) {
                                sendDefaultShop(event, message);
                            } else {
                                message.delete().and(event.getMessage().delete()).queue();
                            }
                            waiter.stopWaiting(false);
                        });
                    });
                }
                reactionWaiter.stopWaiting(false);
            });
        };

        if (shopMessage == null) {
            event.reply(embedBuilder, success);
        } else {
            shopMessage.editMessage(embedBuilder.build()).queue(success);
        }
    }

    private Optional<String> buy(Member member, Item item) {
        BotUser botUser = userService.getById(member.getIdLong());

        if (userService.hasItem(member.getIdLong(), item.getItemId())) {
            return Optional.of("Du besitzt dieses Item bereits!");
        }

        if (botUser.getCoins() < item.getPrice()) {
            return Optional.of("Du hast nicht genug Münzen!");
        }

        userService.buyItem(botUser.getUserId(), item.getItemId());

        return Optional.empty();
    }

    private void generateEmbeds() {
        shopOverview = embedCache.getEmbed("shopOverview").toEmbedBuilder();

        for (ItemCategory itemCategory : ItemCategory.values()) {
            EmbedBuilder specificShop = embedCache.getEmbed("specificShop").toEmbedBuilder();
            List<Item> items = levelService.getItemsByCategoryId(itemCategory.id);
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                if (!item.isVisible()) {
                    continue;
                }
                specificShop.addField(
                        String.format("%s: %s", EmoteType.getNumber(i + 1).unicode, item.getName()),
                        String.format("Preis: %s :moneybag:\nDauer: %d Tage", item.getPrice(), TimeUnit.MILLISECONDS.toDays(item.getDuration())),
                        false
                );
            }
            specificShops.put(itemCategory, specificShop);
        }
    }

    private enum ItemCategory {
        PREMIUM(0),
        DJ(1),
        NICKNAME(2),
        COINS_BOOSTER(3),
        XP_BOOSTER(4);

        public final int id;

        ItemCategory(int id) {
            this.id = id;
        }
    }
}