package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.shop.data.items.FrozenItem;
import de.kaktushose.levelbot.shop.data.items.FrozenItemRepository;
import de.kaktushose.levelbot.shop.data.items.Item;
import de.kaktushose.levelbot.shop.data.items.ItemRepository;
import de.kaktushose.levelbot.shop.data.transactions.Transaction;
import de.kaktushose.levelbot.shop.data.transactions.TransactionRepository;
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
    private final FrozenItemRepository frozenItemRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        frozenItemRepository = context.getBean(FrozenItemRepository.class);
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
        return userRepository.getAllWithDaily();
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

    public void addUpItem(long userId, int itemId, Levelbot levelbot) {
        Item itemToAdd = itemRepository.findById(itemId).orElseThrow();

        List<Item> items = getItems(userId);
        Optional<Item> optional = items.stream().filter(item -> item.getCategoryId() == itemToAdd.getCategoryId()).findFirst();

        // premium unlimited
        if (itemId == 3) {
            // if user has premium: freeze current premium, else go ahead
            if (optional.isPresent()) {
                Transaction transaction = transactionRepository.findByUserIdAndItemId(userId, optional.get().getItemId()).stream().findFirst().orElseThrow();

                FrozenItem frozenItem = new FrozenItem(userId, System.currentTimeMillis(), transaction.getBuyTime(), transaction.getItem());
                frozenItemRepository.save(frozenItem);

                // remove normal premium for now
                removeItem(userId, optional.get().getItemId(), levelbot);

                // make sure that premium unlimited is added as a new item
                optional = Optional.empty();
            }
        }

        BotUser botUser = getUserById(userId);
        Transaction transaction;
        if (optional.isPresent()) {
            transaction = transactionRepository.findByUserIdAndItemId(userId, optional.get().getItemId()).stream().findFirst().orElseThrow();
            transaction.setBuyTime(transaction.getBuyTime() + itemToAdd.getDuration());
        } else {
            transaction = new Transaction();
            transaction.setBuyTime(System.currentTimeMillis());
            transaction.setItem(itemToAdd);
            levelbot.addItemRole(userId, itemToAdd.getItemId());
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
        List<Integer> userItems = getItems(userId).stream().map(Item::getItemId).collect(Collectors.toList());
        return itemRepository.findByCategoryId(categoryId).stream().anyMatch(item -> userItems.contains(item.getItemId()));
    }

    public void removeItem(long userId, int itemId, Levelbot levelbot) {
        Optional<Transaction> optional = transactionRepository.findByUserIdAndItemId(userId, itemId).stream().findFirst();

        // premium unlimited
        if (itemId == 3) {
            // unfreeze item
            Optional<FrozenItem> frozenItemOptional = frozenItemRepository.findById(userId);
            if (frozenItemOptional.isPresent()) {
                FrozenItem frozenItem = frozenItemOptional.get();
                optional.ifPresent(transactionRepository::delete);

                Transaction transaction = new Transaction();
                BotUser botUser = getUserById(userId);
                transaction.setBuyTime(frozenItem.getBuyTime() + (System.currentTimeMillis() - frozenItem.getStartTime()));
                transaction.setItem(frozenItem.getItem());
                botUser.getTransactions().add(transaction);

                frozenItemRepository.delete(frozenItem);
                transactionRepository.save(transaction);
                userRepository.save(botUser);
                return;
            }
        }

        optional.ifPresent(transactionRepository::delete);
        levelbot.removeItemRole(userId, itemId);
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

    public void updateStatistics(long userId) {
        System.out.println("digga was");
        BotUser botUser = getUserById(userId);
        botUser.setStartCoins(botUser.getCoins());
        botUser.setStartXp(botUser.getXp());
        botUser.setStartDiamonds(botUser.getDiamonds());
        userRepository.save(botUser);
        System.out.println("ey");
    }

    public int setRank(long userId, int rank) {
        BotUser botUser = getUserById(userId);
        if (botUser.getLevel() == 13) {
            return 13;
        }
        botUser.setLevel(rank);
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
