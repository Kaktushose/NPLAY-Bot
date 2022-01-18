package de.kaktushose.levelbot.shop.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.LevelService;
import de.kaktushose.levelbot.database.services.UserService;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.Item;
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

@CommandController("additem")
@Permission("moderator")
public class AddItemCommand {

    private static final String BACK = "◀️";
    private static final String CANCEL = "❌";
    private static final String PREMIUM = "⭐";
    private static final String DJ = "\uD83C\uDFB5";
    private static final String NICKNAME = "\uD83D\uDC68\uD83C\uDFFD";
    private static final String COIN_BOOSTER = "\uD83D\uDCB0";
    private static final String XP_BOOSTER = "\uD83C\uDF1F";
    private final Map<ItemCategory, EmbedBuilder> specificShops;
    @Inject
    private UserService userService;
    @Inject
    private ShopService shopService;
    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;
    @Inject
    private Levelbot levelbot;
    private EmbedBuilder shopOverview;

    public AddItemCommand() {
        specificShops = new HashMap<>();
    }

    @Command(
            name = "Level-Shop",
            usage = "{prefix}additem <member>",
            desc = "Fügt ein Item einem anderen Nutzer hinzu",
            category = "Moderation"
    )
    public void onAddItem(CommandEvent event, Member member) {
        generateEmbeds(member.getAsMention());
        sendDefaultShop(event, null, member);
    }

    private void sendDefaultShop(CommandEvent event, Message shopMessage, Member target) {
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
                        sendSpecificShop(event, message, target, ItemCategory.PREMIUM);
                        break;
                    case DJ:
                        sendSpecificShop(event, message, target, ItemCategory.DJ);
                        break;
                    case NICKNAME:
                        sendSpecificShop(event, message, target, ItemCategory.NICKNAME);
                        break;
                    case COIN_BOOSTER:
                        sendSpecificShop(event, message, target, ItemCategory.COINS_BOOSTER);
                        break;
                    case XP_BOOSTER:
                        sendSpecificShop(event, message, target, ItemCategory.XP_BOOSTER);
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

    private void sendSpecificShop(CommandEvent event, Message shopMessage, Member target, ItemCategory itemCategory) {
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
                        sendDefaultShop(event, message, target);
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
                Optional<String> buyResult = addItem(target, item);

                if (buyResult.isEmpty()) {
                    message.editMessage(embedCache.getEmbed("addItemSuccess")
                            .injectValue("user", target.getAsMention())
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
                                sendDefaultShop(event, message, target);
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

    private Optional<String> addItem(Member member, Item item) {
        BotUser botUser = userService.getUserById(member.getIdLong());

        if (shopService.hasItem(member.getIdLong(), item.getItemId())) {
            return Optional.of(member.getAsMention() + " besitzt dieses Item bereits!");
        }

        shopService.addItem(botUser.getUserId(), item.getItemId());

        return Optional.empty();
    }

    private void generateEmbeds(String mention) {
        shopOverview = embedCache.getEmbed("shopOverview").injectValue("user", mention).toEmbedBuilder();

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