package de.kaktushose.levelbot.database.service;

import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.database.repositories.ItemRepository;
import de.kaktushose.levelbot.database.repositories.TransactionRepository;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
    }

    public List<BotUser> getAll() {
        List<BotUser> result = new ArrayList<>();
        userRepository.findAll().forEach(result::add);
        return result;
    }

    public BotUser getById(long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    public List<BotUser> getByPermission(int permissionLevel) {
        return userRepository.findByPermissionLevel(permissionLevel);
    }

    public BotUser create(long userId) {
        return userRepository.save(new BotUser(userId));
    }

    public BotUser createIfAbsent(long userId) {
        return userRepository.findById(userId).orElse(create(userId));
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public void exchangeDiamonds(long userId, long diamonds) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(botUser.getDiamonds() - diamonds);
        botUser.setCoins(botUser.getCoins() + diamonds * 40);
        userRepository.save(botUser);
    }

    public boolean hasItem(long userId, int itemId) {
        return transactionRepository.findByItemIdAndUserId(itemId, userId).isPresent();
    }

    public void buyItem(long userId, int itemId) {
        BotUser botUser = getById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        Transaction transaction = new Transaction();
        transaction.setBuyTime(System.currentTimeMillis());
        transaction.setItem(item);
        botUser.getTransactions().add(transaction);
        botUser.setCoins(botUser.getCoins() - item.getPrice());
        transactionRepository.save(transaction);
        userRepository.save(botUser);
    }

    public List<Item> getItems(long userId) {
        BotUser botUser = getById(userId);
        return botUser.getTransactions().stream().map(Transaction::getItem).collect(Collectors.toList());
    }

    public void removeItem(long userId, int itemId) {
        BotUser botUser = getById(userId);
        botUser.getTransactions().removeIf(transaction -> transaction.getItem().getItemId() == itemId);
        userRepository.save(botUser);
    }

    public boolean switchDaily(long userId) {
        BotUser botUser = getById(userId);
        botUser.setDailyUpdate(!botUser.isDailyUpdate());
        return userRepository.save(botUser).isDailyUpdate();
    }

    public void setPermission(long userId, int permissionLevel) {
        BotUser botUser = getById(userId);
        botUser.setPermissionLevel(permissionLevel);
        userRepository.save(botUser);
    }

    public void addCoins(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setCoins(botUser.getCoins() + amount);
        userRepository.save(botUser);
    }

    public void addXp(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setXp(botUser.getXp() + amount);
        userRepository.save(botUser);
    }

    public void addDiamonds(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(botUser.getDiamonds() + amount);
        userRepository.save(botUser);
    }

    public void setCoins(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setCoins(amount);
        userRepository.save(botUser);
    }

    public void setXp(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setXp(amount);
        userRepository.save(botUser);
    }

    public void setDiamonds(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(amount);
        userRepository.save(botUser);
    }

}
