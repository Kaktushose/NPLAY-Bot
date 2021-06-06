package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.database.repositories.*;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final NitroBoosterRepository nitroBoosterRepository;
    private final SettingsRepository settingsRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        nitroBoosterRepository = context.getBean(NitroBoosterRepository.class);
        settingsRepository = context.getBean(SettingsRepository.class);
    }

    public List<BotUser> getAllUsers() {
        List<BotUser> result = new ArrayList<>();
        userRepository.findAll().forEach(result::add);
        return result;
    }

    public BotUser getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    public List<BotUser> getUsersByPermission(int permissionLevel) {
        return userRepository.findByPermissionLevel(permissionLevel);
    }

    public List<BotUser> getUsersByDailyEnabled() {
        return userRepository.findByDailyUpdate(true);
    }

    public BotUser createUser(long userId) {
        return userRepository.save(new BotUser(userId));
    }

    public BotUser createUserIfAbsent(long userId) {
        Optional<BotUser> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return createUser(userId);
        }
        return optional.get();
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    public void exchangeDiamonds(long userId, long diamonds) {
        BotUser botUser = getUserById(userId);
        botUser.setDiamonds(botUser.getDiamonds() - diamonds);
        botUser.setCoins(botUser.getCoins() + diamonds * 20);
        userRepository.save(botUser);
    }

    public boolean hasItem(long userId, int itemId) {
        int categoryId = itemRepository.findById(itemId).orElseThrow().getCategoryId();
        return getItems(userId).stream().anyMatch(item -> item.getCategoryId() == categoryId);
    }

    public void buyItem(long userId, int itemId) {
        BotUser botUser = getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        Transaction transaction = new Transaction();
        transaction.setBuyTime(System.currentTimeMillis());
        transaction.setItem(item);
        botUser.getTransactions().add(transaction);
        botUser.setCoins(botUser.getCoins() - item.getPrice());
        transactionRepository.save(transaction);
        userRepository.save(botUser);
    }

    public void addUpItem(long userId, int itemId) {
        BotUser botUser = getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        Optional<Transaction> optional = transactionRepository.findByUserIdAndItemId(userId, itemId);
        Transaction transaction;
        if (optional.isPresent()) {
            transaction = optional.get();
            transaction.setBuyTime(transaction.getBuyTime() + item.getDuration());
        } else {
            transaction = new Transaction();
            transaction.setBuyTime(System.currentTimeMillis());
            transaction.setItem(item);
        }
        botUser.getTransactions().add(transaction);
        transactionRepository.save(transaction);
        userRepository.save(botUser);
    }

    public List<Item> getItems(long userId) {
        BotUser botUser = getUserById(userId);
        return botUser.getTransactions().stream().map(Transaction::getItem).collect(Collectors.toList());
    }

    public boolean ownsItemOfCategory(long userId, int categoryId) {
        List<Item> userItems = getItems(userId);
        return itemRepository.findByCategoryId(categoryId).stream().anyMatch(userItems::contains);
    }

    public void removeItem(long userId, int itemId) {
        Optional<Transaction> optional = transactionRepository.findByUserIdAndItemId(userId, itemId);
        optional.ifPresent(transactionRepository::delete);
    }

    public boolean switchDaily(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setDailyUpdate(!botUser.isDailyUpdate());
        return userRepository.save(botUser).isDailyUpdate();
    }

    public void setPermission(long userId, int permissionLevel) {
        BotUser botUser = getUserById(userId);
        botUser.setPermissionLevel(permissionLevel);
        userRepository.save(botUser);
    }

    public long addCoins(long userId, long amount) {
        BotUser botUser = getUserById(userId);
        botUser.setCoins(botUser.getCoins() + amount);
        userRepository.save(botUser);
        return botUser.getCoins();
    }

    public long addXp(long userId, long amount) {
        BotUser botUser = getUserById(userId);
        botUser.setXp(botUser.getXp() + amount);
        userRepository.save(botUser);
        return botUser.getXp();
    }

    public long addDiamonds(long userId, long amount) {
        BotUser botUser = getUserById(userId);
        botUser.setDiamonds(botUser.getDiamonds() + amount);
        userRepository.save(botUser);
        return botUser.getDiamonds();
    }

    public void setCoins(long userId, int amount) {
        BotUser botUser = getUserById(userId);
        botUser.setCoins(amount);
        userRepository.save(botUser);
    }

    public void setXp(long userId, int amount) {
        BotUser botUser = getUserById(userId);
        botUser.setXp(amount);
        userRepository.save(botUser);
    }

    public void setDiamonds(long userId, int amount) {
        BotUser botUser = getUserById(userId);
        botUser.setDiamonds(amount);
        userRepository.save(botUser);
    }

    public void updateLastValidMessage(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setLastValidMessage(System.currentTimeMillis());
        userRepository.save(botUser);
    }

    public void updateMessageCount(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setMessageCount(botUser.getMessageCount() + 1);
        userRepository.save(botUser);
    }

    public int increaseRank(long userId) {
        BotUser botUser = getUserById(userId);
        if (botUser.getLevel() == 10) {
            return 10;
        }
        botUser.setLevel(botUser.getLevel() + 1);
        userRepository.save(botUser);
        return botUser.getLevel();
    }

    public int increaseRewardLevel(long userId) {
        BotUser botUser = getUserById(userId);
        int newLevel = botUser.getRewardLevel() + 1;
        newLevel = newLevel > 7 ? 1 : newLevel;
        botUser.setRewardLevel(newLevel);
        userRepository.save(botUser);
        return newLevel;
    }

    public int resetRewardLevel(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setRewardLevel(1);
        userRepository.save(botUser);
        return 1;
    }

    public void updateLastReward(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setLastReward(System.currentTimeMillis());
        userRepository.save(botUser);
    }

    public void resetEventPoints(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setEventPoints(0);
        userRepository.save(botUser);
    }

    public long increaseEventPoints(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setEventPoints(botUser.getEventPoints() + 1);
        return userRepository.save(botUser).getEventPoints();
    }
}
