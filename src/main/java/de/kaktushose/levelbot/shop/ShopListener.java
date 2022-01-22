package de.kaktushose.levelbot.shop;

import com.github.kaktushose.jda.commands.api.EmbedCache;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.LevelService;
import de.kaktushose.levelbot.database.services.UserService;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.Item;
import de.kaktushose.levelbot.util.NumberEmojis;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;

public class ShopListener extends ListenerAdapter {

    // this should find it's way into the database one day
    public static final String PREMIUM_MESSAGE_ID = "851454666591698965";
    public static final String DJ_MESSAGE_ID = "851454738904907827";
    public static final String NICKNAME_MESSAGE_ID = "851454813623681024";
    public static final String COINS_BOOSTER_MESSAGE_ID = "851454959224225813";
    public static final String XP_BOOSTER_MESSAGE_ID = "851454895788654625";
    private static final Long LEVEL_SYSTEM_CHANNEL_ID = 851388807239827466L;
    private static final String CONFIRM = "✅";
    private static final String CANCEL = "❌";
    private final Set<Long> activeUsers;
    private final UserService userService;
    private final ShopService shopService;
    private final LevelService levelService;
    private final EmbedCache embedCache;
    private final Levelbot levelbot;

    public ShopListener(Levelbot levelbot) {
        this.userService = levelbot.getUserService();
        this.shopService = levelbot.getShopService();
        this.levelService = levelbot.getLevelService();
        this.embedCache = levelbot.getEmbedCache();
        this.levelbot = levelbot;
        this.activeUsers = new HashSet<>();
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        // bots should just be ignored
        if (event.getUser().isBot()) {
            return;
        }

        // must be in channel #levelsystem
        if (event.getChannel().getIdLong() != LEVEL_SYSTEM_CHANNEL_ID) {
            return;
        }

        if (levelbot.getJdaCommands().getDefaultSettings().getMutedUsers().contains(event.getUser().getIdLong())) {
            event.getReaction().removeReaction(event.getUser()).queue(
                    null, new ErrorHandler().ignore(UNKNOWN_MESSAGE)
            );
            return;
        }

        // indicates active purchase
        if (activeUsers.contains(event.getUser().getIdLong())) {
            event.getReaction().removeReaction(event.getUser()).queue(
                    null, new ErrorHandler().ignore(UNKNOWN_MESSAGE)
            );
            return;
        }

        Member member = event.getMember();
        ItemCategory itemCategory;
        switch (event.getMessageId()) {
            case PREMIUM_MESSAGE_ID:
                itemCategory = ItemCategory.PREMIUM;
                break;
            case DJ_MESSAGE_ID:
                itemCategory = ItemCategory.DJ;
                break;
            case NICKNAME_MESSAGE_ID:
                itemCategory = ItemCategory.NICKNAME;
                break;
            case COINS_BOOSTER_MESSAGE_ID:
                itemCategory = ItemCategory.COINS_BOOSTER;
                break;
            case XP_BOOSTER_MESSAGE_ID:
                itemCategory = ItemCategory.XP_BOOSTER;
                break;
            default:
                event.getReaction().removeReaction(member.getUser()).queue(
                        null, new ErrorHandler().ignore(UNKNOWN_MESSAGE)
                );
                return;
        }

        int variant;
        switch (event.getReactionEmote().getName()) {
            case NumberEmojis.ONE:
                variant = 0;
                break;
            case NumberEmojis.TWO:
                variant = 1;
                break;
            case NumberEmojis.THREE:
                variant = 2;
                break;
            default:
                event.getReaction().removeReaction(member.getUser()).queue();
                return;
        }

        if ((itemCategory == ItemCategory.COINS_BOOSTER || itemCategory == ItemCategory.XP_BOOSTER) && variant == 2) {
            event.getReaction().removeReaction(member.getUser()).queue();
            return;
        }

        Item item = levelService.getItemsByCategoryId(itemCategory.id).get(variant);
        BotUser botUser = userService.getUserById(member.getIdLong());
        String fail = null;
        if (shopService.hasItemOfCategory(member.getIdLong(), item.getCategoryId())) {
            fail = "Du besitzt dieses Item bereits!";
        }
        if (botUser.getCoins() < item.getPrice()) {
            fail = "Du hast nicht genug Münzen!";
        }

        TextChannel channel = event.getChannel();

        Consumer<Message> delete = success -> success.delete().queueAfter(
                30, TimeUnit.SECONDS, null, new ErrorHandler().ignore(UNKNOWN_MESSAGE)
        );

        MessageBuilder builder = new MessageBuilder().append(member.getAsMention());

        if (fail != null) {
            channel.sendMessage( // transaction failed
                    builder.setEmbeds(embedCache.getEmbed("shopError")
                            .injectValue("message", fail)
                            .toMessageEmbed()
                    ).build()
            ).queue(delete);
        } else {
            activeUsers.add(member.getIdLong());
            channel.sendMessage( // confirm transaction
                    builder.setEmbeds(embedCache.getEmbed("shopConfirm")
                            .injectValue("item", item.getName())
                            .injectValue("price", item.getPrice())
                            .toMessageEmbed()
                    ).build()
            ).queue(confirmMessage -> { // wait for reactions
                confirmMessage.delete().queueAfter( // delete confirm message after 30 secs if nothing happens
                        30, TimeUnit.SECONDS,
                        success -> activeUsers.remove(member.getIdLong()),
                        new ErrorHandler().ignore(UNKNOWN_MESSAGE)
                );
                confirmMessage.addReaction(CONFIRM).and(confirmMessage.addReaction(CANCEL)).queue();
                ReactionWaiter waiter = new ReactionWaiter(confirmMessage, event.getMember(), CONFIRM, CANCEL);
                waiter.onEvent(reactionEvent -> { // on reaction confirm or cancel emoji
                    confirmMessage.delete().queue();
                    if (reactionEvent.getEmote().equals(CONFIRM)) {
                        shopService.buyItem(botUser.getUserId(), item.getItemId());
                        channel.sendMessageEmbeds( // successful transaction
                                embedCache.getEmbed("shopSuccess")
                                        .injectValue("item", item.getName())
                                        .injectValue("days", TimeUnit.MILLISECONDS.toDays(item.getDuration()))
                                        .toMessageEmbed()
                        ).queue(delete);
                        levelbot.getLogChannel().sendMessageEmbeds(embedCache.getEmbed("buyLog")
                                .injectValue("user", member.getAsMention())
                                .injectValue("item", item.getName())
                                .toMessageEmbed()
                        ).queue();
                    }
                    activeUsers.remove(member.getIdLong());
                    waiter.stopWaiting(false);
                });
            });
        }
        event.getReaction().removeReaction(member.getUser()).queue();
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
