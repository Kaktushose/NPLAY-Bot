package de.kaktushose.levelbot.shop.data;

import de.kaktushose.levelbot.Levelbot;
import de.kaktushose.levelbot.account.data.BotUser;
import de.kaktushose.levelbot.account.data.UserRepository;
import de.kaktushose.levelbot.account.data.UserService;
import de.kaktushose.levelbot.shop.data.items.*;
import de.kaktushose.levelbot.shop.data.transactions.Transaction;
import de.kaktushose.levelbot.shop.data.transactions.TransactionRepository;
import de.kaktushose.levelbot.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShopService {

    private final static Logger log = LoggerFactory.getLogger(ShopService.class);
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final FrozenItemRepository frozenItemRepository;
    private final UserService userService;
    private final Levelbot levelbot;

    public ShopService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        frozenItemRepository = context.getBean(FrozenItemRepository.class);
        userService = levelbot.getUserService();
        this.levelbot = levelbot;
    }

    public boolean hasItem(long userId, int itemId) {
        return transactionRepository.existsByUserIdAndItemId(userId, itemId);
    }

    public boolean hasItemOfCategory(long userId, ItemCategory category) {
        return hasItemOfCategory(userId, category.getCategoryId());
    }

    public boolean hasItemOfCategory(long userId, int category) {
        return transactionRepository.existsByUserIdAndCategoryId(userId, category);
    }

    public List<Item> getItems(long userId) {
        return itemRepository.findItemsByUserId(userId);
    }

    public void buyItem(long userId, int itemId) {
        BotUser botUser = userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        transactionRepository.save(addItemToUser(botUser, item));
        botUser.setCoins(botUser.getCoins() - item.getPrice());
        userRepository.save(botUser);
        levelbot.addItemRole(botUser.getUserId(), item.getItemId());
    }

    public void addItem(long userId, ItemCategory category, ItemVariant variant) {
        addItem(userId, category.getItemId(variant));
    }

    public void addItem(long userId, int itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow();

        if (itemId == 3) {
            if (hasItemOfCategory(userId, ItemCategory.PREMIUM)) {
                frozenItemRepository.save(FrozenItem.fromTransaction(userId, getTransaction(userId, itemId)));
                removeItem(userId, itemId);
            }
        }

        BotUser botUser = userService.getUserById(userId);
        Transaction transaction;
        if (hasItemOfCategory(userId, item.getCategoryId())) {
            // TODO add getItemByCategoryId method
            int id = getItems(userId).stream().filter(i -> i.getCategoryId() == item.getCategoryId())
                    .findFirst()
                    .orElseThrow()
                    .getItemId();
            transaction = getTransaction(userId, id);
            transaction.setBuyTime(transaction.getBuyTime() + item.getDuration());
        } else {
            transaction = addItemToUser(botUser, item);
            levelbot.addItemRole(userId, item.getItemId());
        }

        transactionRepository.save(transaction);
        userRepository.save(botUser);
    }

    private Transaction addItemToUser(BotUser botUser, Item item) {
        Transaction transaction = new Transaction();
        transaction.setBuyTime(System.currentTimeMillis());
        transaction.setItem(item);
        transaction.setUserId(botUser.getUserId());
        botUser.getTransactions().add(transaction);
        return transaction;
    }

    public Transaction getTransaction(long userId, int itemId) {
        return transactionRepository.findByUserIdAndItemId(userId, itemId).stream().findFirst().orElseThrow();
    }

    public void removeItem(long userId, ItemCategory category, ItemVariant variant) {
        removeItem(userId, category.getItemId(variant));
    }

    public void removeItem(long userId, int itemId) {
        transactionRepository.deleteByUserIdAndItemId(userId, itemId);
        BotUser botUser = userService.getUserById(userId);

        if (itemId == 3) {
            if (frozenItemRepository.existsById(userId)) {
                FrozenItem frozenItem = frozenItemRepository.findById(userId).orElseThrow();

                Transaction transaction = new Transaction();
                transaction.setBuyTime(
                        frozenItem.getBuyTime() + (System.currentTimeMillis() - frozenItem.getStartTime())
                );
                transaction.setItem(frozenItem.getItem());
                botUser.getTransactions().add(transaction);

                frozenItemRepository.delete(frozenItem);
                transactionRepository.save(transaction);
                userRepository.save(botUser);
                return;
            }
        }

        levelbot.removeItemRole(userId, itemId);
    }

    public void checkForExpiredItems() {
        for (Transaction transaction : transactionRepository.findExpiringTransactions()) {
            Item item = transaction.getItem();
            long remaining = item.getRemainingTimeAsLong(transaction.getBuyTime());
            long userId = transaction.getUserId();
            int itemId = item.getItemId();

            if (itemId == ItemCategory.PREMIUM.getItemId(ItemVariant.UNLIMITED)) {
                continue;
            }
            if (remaining <= 0) {
                removeItem(userId, itemId);
                levelbot.sendItemExpiredInformation(userId, itemId, transaction.getBuyTime());
            } else if (remaining < 86400000) {
                levelbot.getTaskScheduler().addSingleTask(() -> {
                    try {
                        removeItem(userId, itemId);
                        levelbot.sendItemExpiredInformation(userId, itemId, transaction.getBuyTime());
                    } catch (Throwable t) {
                        log.error("An exception has occurred while removing an item!", t);
                    }
                }, remaining, TimeUnit.MILLISECONDS);
            }
        }
    }
}
