package de.kaktushose.levelbot.database.service;

import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.repositories.ItemRepository;
import de.kaktushose.levelbot.database.repositories.RankRepository;
import de.kaktushose.levelbot.database.repositories.TransactionRepository;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class LevelService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;

    public LevelService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        rankRepository = context.getBean(RankRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
    }

    public Rank getRank(int rankId) {
        return rankRepository.findById(rankId).orElseThrow();
    }

    public Rank getCurrentRank(long userId) {
        BotUser botUser = userRepository.findById(userId).orElseThrow();
        return rankRepository.findById(botUser.getLevel()).orElseThrow();
    }

    public Rank getNextRank(long userId) {
        BotUser botUser = userRepository.findById(userId).orElseThrow();
        return rankRepository.findById(botUser.getLevel() + 1).orElseThrow();
    }

    public List<Item> getItemsByCategoryId(int categoryId) {
        return itemRepository.findByCategoryId(categoryId);
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

}
