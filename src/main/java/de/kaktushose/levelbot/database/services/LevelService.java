package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.*;
import de.kaktushose.levelbot.database.repositories.ChancesRepository;
import de.kaktushose.levelbot.database.repositories.ItemRepository;
import de.kaktushose.levelbot.database.repositories.RankRepository;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;

import javax.xml.crypto.Data;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class LevelService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final ItemRepository itemRepository;
    private final ChancesRepository chancesRepository;
    private final UserService userService;
    private final SettingsService settingsService;
    private final Levelbot levelbot;

    public LevelService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        rankRepository = context.getBean(RankRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        chancesRepository = context.getBean(ChancesRepository.class);
        this.userService = levelbot.getUserService();
        this.settingsService = levelbot.getSettingsService();
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
        if (userService.ownsItemOfCategory(userId, 3)) {
            coins += 2;
        }
        long xp = randomXp();
        if (userService.ownsItemOfCategory(userId, 4)) {
            coins += 2;
        }

        userService.addDiamonds(userId, diamonds);
        userService.addCoins(userId, coins);
        long newXp = userService.addXp(userId, xp);

        if (getCurrentRank(userId).getRankId() == 13) {
            return Optional.empty();
        }

        if (newXp < getNextRank(userId).getBound()) {
            return Optional.empty();
        }

        Rank rank = rankRepository.getRankByXp(newXp);

        return Optional.of(getRank(userService.setRank(userId, rank.getRankId())));
    }

    public Optional<String> getDailyReward(long userId) {
        BotUser botUser = userService.getUserById(userId);
        int rewardLevel;

        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime lastReward = ZonedDateTime.ofInstant(Instant.ofEpochMilli(botUser.getLastReward()), ZoneId.of(ZoneId.systemDefault().getId())).truncatedTo(ChronoUnit.DAYS);
        if (now.equals(lastReward)) {
            return Optional.empty();
        }
        now = now.minus(1, ChronoUnit.DAYS);
        if (!now.equals(lastReward)) {
            rewardLevel = userService.resetRewardLevel(userId);
        } else {
            rewardLevel = userService.increaseRewardLevel(userId);
        }

        Reward reward = settingsService.getReward(rewardLevel);
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        userService.updateLastReward(userId);
        if (reward.getItem() != null) {
            userService.addUpItem(userId, reward.getItem().getItemId(), levelbot);
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
                userService.addUpItem(userId, rankReward.getItem().getItemId(), levelbot);
            }
            rewardText.append(rankReward.getMessage()).append("\n");
        });
        return rewardText.substring(0, rewardText.length() - 1);
    }
}
