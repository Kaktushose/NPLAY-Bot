package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.CurrencyChance;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.database.repositories.ChancesRepository;
import de.kaktushose.levelbot.database.repositories.RankRepository;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.Item;
import de.kaktushose.levelbot.shop.data.items.ItemCategory;
import de.kaktushose.levelbot.shop.data.items.ItemRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class LevelService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final ItemRepository itemRepository;
    private final ChancesRepository chancesRepository;
    private final UserService userService;
    private final SettingsService settingsService;
    private final ShopService shopService;
    private final Levelbot levelbot;

    public LevelService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        rankRepository = context.getBean(RankRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        chancesRepository = context.getBean(ChancesRepository.class);
        this.userService = levelbot.getUserService();
        this.settingsService = levelbot.getSettingsService();
        shopService = levelbot.getShopService();
        this.levelbot = levelbot;
    }

    public Rank getRank(int rankId) {
        return rankRepository.findById(rankId).orElseThrow();
    }

    public Rank getPreviousRank(long userId) {
        BotUser botUser = userService.getUserById(userId);
        if (botUser.getLevel() == 1) {
            return getRank(1);
        }
        return getRank(botUser.getLevel() - 1);
    }

    public Rank getCurrentRank(long userId) {
        BotUser botUser = userService.getUserById(userId);
        return getRank(botUser.getLevel());
    }

    public Rank getNextRank(long userId) {
        BotUser botUser = userService.getUserById(userId);
        if (botUser.getLevel() == 13) {
            return getRank(13);
        }
        return getRank(botUser.getLevel() + 1);
    }

    public List<Item> getItemsByCategoryId(int categoryId) {
        return itemRepository.findByCategoryId(categoryId);
    }

    public Item getItem(int itemId) {
        return itemRepository.findById(itemId).orElseThrow();
    }

    public boolean setItemPrice(int itemId, int price) {
        if (!itemRepository.existsById(itemId)) {
            return false;
        }
        Item item = getItem(itemId);
        item.setPrice(price);
        itemRepository.save(item);
        return true;
    }

    public Pagination getXpLeaderboard(int pageSize, JDA jda) {
        return new Pagination(pageSize, userRepository.getXpLeaderboard(), jda, Pagination.CurrencyType.XP);
    }

    public Pagination getCoinsLeaderboard(int pageSize, JDA jda) {
        return new Pagination(pageSize, userRepository.getCoinsLeaderboard(), jda, Pagination.CurrencyType.COINS);
    }

    public Pagination getDiamondsLeaderboard(int pageSize, JDA jda) {
        return new Pagination(pageSize, userRepository.getDiamondsLeaderboard(), jda, Pagination.CurrencyType.DIAMONDS);
    }

    public boolean isValidMessage(long userId, long guildId, long channelId) {
        BotUser botUser = userService.getUserById(userId);
        if (settingsService.isIgnoredChannel(channelId)) {
            return false;
        }
        if (botUser.getPermissionLevel() < 1) {
            return false;
        }
        return System.currentTimeMillis() - botUser.getLastValidMessage() >= settingsService.getMessageCooldown(guildId);
    }

    public long randomXp() {
        List<CurrencyChance> chances = chancesRepository.getXpChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

    public long randomCoins() {
        List<CurrencyChance> chances = chancesRepository.getCoinChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

    public long randomDiamonds() {
        List<CurrencyChance> chances = chancesRepository.getDiamondChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

    public Optional<Rank> onValidMessage(long userId) {
        userService.updateLastValidMessage(userId);
        userService.updateMessageCount(userId);

        long diamonds = randomDiamonds();
        long coins = randomCoins();
        if (shopService.hasItemOfCategory(userId, ItemCategory.COIN_BOOSTER)) {
            coins += 2;
        }
        long xp = randomXp();
        if (shopService.hasItemOfCategory(userId, ItemCategory.XP_BOOSTER)) {
            xp += 2;
        }

        userService.addDiamonds(userId, diamonds);
        userService.addCoins(userId, coins);
        long newXp = userService.addXp(userId, xp);

        if (getCurrentRank(userId).getRankId() == 13) {
            return Optional.empty();
        }

        System.out.println(newXp);
        System.out.println(getNextRank(userId).getBound());
        if (newXp < getNextRank(userId).getBound()) {
            return Optional.empty();
        }

        Rank rank = rankRepository.getRankByXp(newXp);

        return Optional.of(getRank(userService.setRank(userId, rank.getRankId())));
    }

    public Optional<String> getDailyReward(long userId) {
        BotUser botUser = userService.getUserById(userId);

        int rewardLevel;
        if (System.currentTimeMillis() - botUser.getLastReward() >= 172800000L) {
            rewardLevel = userService.resetRewardLevel(userId);
        } else if (System.currentTimeMillis() - botUser.getLastReward() >= 86400000L) {
            rewardLevel = userService.increaseRewardLevel(userId);
        } else {
            return Optional.empty();
        }

        Reward reward = settingsService.getReward(rewardLevel);
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        userService.updateLastReward(userId);
        if (reward.getItem() != null) {
            shopService.addItem(userId, reward.getItem().getItemId());
        }

        return Optional.of(reward.getMessage());
    }

    public String applyRewards(long userId, int rankId) {
        Rank rank = getRank(rankId);
        StringBuilder rewardText = new StringBuilder();
        rank.getRankRewards().forEach(rankReward -> {
            userService.addCoins(userId, rankReward.getCoins());
            userService.addDiamonds(userId, rankReward.getDiamonds());
            userService.addXp(userId, rankReward.getXp());
            if (rankReward.getItem() != null) {
                shopService.addItem(userId, rankReward.getItem().getItemId());
            }
            rewardText.append(rankReward.getMessage()).append("\n");
        });
        return rewardText.substring(0, rewardText.length() - 1);
    }
}
