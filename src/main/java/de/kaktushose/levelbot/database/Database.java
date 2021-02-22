package de.kaktushose.levelbot.database;

import de.kaktushose.levelbot.database.model.CurrencyChance;
import de.kaktushose.levelbot.database.model.Config;
import de.kaktushose.levelbot.database.repositories.*;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Database {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final TransactionRepository transactionRepository;
    private final ChancesRepository chancesRepository;
    private final MutedChannelRepository mutedChannelRepository;
    private final SettingsRepository settingsRepository;
    private final RankRepository rankRepository;

    public Database() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        itemCategoryRepository = context.getBean(ItemCategoryRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        chancesRepository = context.getBean(ChancesRepository.class);
        mutedChannelRepository = context.getBean(MutedChannelRepository.class);
        settingsRepository = context.getBean(SettingsRepository.class);
        rankRepository = context.getBean(RankRepository.class);
    }

    public UserRepository getUsers() {
        return userRepository;
    }

    public ItemRepository getItems() {
        return itemRepository;
    }

    public ItemCategoryRepository getItemCategories() {
        return itemCategoryRepository;
    }

    public TransactionRepository getTransactions() {
        return transactionRepository;
    }

    public MutedChannelRepository getMutedChannels() {
        return mutedChannelRepository;
    }

    public RankRepository getRanks() {
        return rankRepository;
    }

    public Config getGuildSettings(long guildId) {
        return settingsRepository.getGuildSettings(guildId);
    }

    public int getNewXp() {
        List<CurrencyChance> chances = chancesRepository.getXpChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

    public int getNewCoins() {
        List<CurrencyChance> chances = chancesRepository.getCoinChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

    public int getNewDiamonds() {
        List<CurrencyChance> chances = chancesRepository.getDiamondChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (CurrencyChance chance : chances) {
            if (random <= chance.getChance()) {
                return chance.getAmount();
            }
        }
        return 0;
    }

}
