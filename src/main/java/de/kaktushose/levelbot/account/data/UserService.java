package de.kaktushose.levelbot.account.data;

import de.kaktushose.levelbot.bot.ApplicationContextHolder;
import de.kaktushose.levelbot.leveling.data.reward.Reward;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class UserService {

    /**
     * the exchange rate for diamonds -> coins
     */
    public static final int EXCHANGE_RATE = 20;
    private final UserRepository userRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
    }

    public BotUser getBotUser(User user) {
        return getBotUser(user.getIdLong());
    }

    public BotUser getBotUser(Member member) {
        return getBotUser(member.getIdLong());
    }

    public BotUser getBotUser(long userId) {
        return userRepository.findById(userId).orElseGet(() -> createUser(userId));
    }

    public BotUser createUser(long userId) {
        return userRepository.save(new BotUser(userId));
    }

    public boolean isMuted(User user) {
        return userRepository.isMuted(user.getIdLong());
    }

    public List<Long> getUsersByPermission(int permissionLevel) {
        return userRepository.findByPermissionLevel(permissionLevel);
    }

    public boolean hasPermission(User user, int permissionLevel) {
        return userRepository.hasPermission(user.getIdLong(), permissionLevel);
    }

    public List<BotUser> getUsersByDailyEnabled() {
        return userRepository.getAllWithDaily();
    }

    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    public void exchangeDiamonds(BotUser botUser, long diamonds) {
        botUser.setDiamonds(botUser.getDiamonds() - diamonds);
        botUser.setCoins(botUser.getCoins() + diamonds * EXCHANGE_RATE);
        userRepository.save(botUser);
    }

    public boolean switchDaily(User user) {
        BotUser botUser = getBotUser(user.getIdLong());
        botUser.setDailyUpdate(!botUser.isDailyUpdate());
        return userRepository.save(botUser).isDailyUpdate();
    }

    public void setPermission(long userId, int permissionLevel) {
        BotUser botUser = getBotUser(userId);
        botUser.setPermissionLevel(permissionLevel);
        userRepository.save(botUser);
    }

    public BotUser addCurrencies(long userId, long coins, long xp, long diamonds) {
        return addCurrencies(getBotUser(userId), coins, xp, diamonds);
    }

    public BotUser addCurrencies(BotUser botUser, long coins, long xp, long diamonds) {
        botUser.setCoins(botUser.getCoins() + coins);
        botUser.setXp(botUser.getXp() + xp);
        botUser.setDiamonds(botUser.getDiamonds() + diamonds);
        return userRepository.save(botUser);
    }

    public BotUser addCurrencies(long userId, Reward reward) {
        return addCurrencies(getBotUser(userId), reward);
    }

    public BotUser addCurrencies(BotUser botUser, Reward reward) {
        botUser.setCoins(botUser.getCoins() + reward.getCoins());
        botUser.setXp(botUser.getXp() + reward.getXp());
        botUser.setDiamonds(botUser.getDiamonds() + reward.getDiamonds());
        return userRepository.save(botUser);
    }

    public void setCoins(long userId, int amount) {
        BotUser botUser = getBotUser(userId);
        botUser.setCoins(amount);
        userRepository.save(botUser);
    }

    public void setXp(long userId, int amount) {
        BotUser botUser = getBotUser(userId);
        botUser.setXp(amount);
        userRepository.save(botUser);
    }

    public void setDiamonds(long userId, int amount) {
        BotUser botUser = getBotUser(userId);
        botUser.setDiamonds(amount);
        userRepository.save(botUser);
    }


    public BotUser onValidMessage(long userId) {
        BotUser botUser = getBotUser(userId);
        botUser.setLastValidMessage(System.currentTimeMillis());
        botUser.setMessageCount(botUser.getMessageCount() + 1);
        return userRepository.save(botUser);
    }

    public void updateUserStatistics() {
        userRepository.updateStatistics();
    }

    public int setRank(BotUser botUser, int rank) {
        if (botUser.getLevel() == 13) {
            return 13;
        }
        botUser.setLevel(rank);
        return  userRepository.save(botUser).getLevel();
    }

    public int increaseRewardLevel(BotUser botUser) {
        int newLevel = botUser.getRewardLevel() + 1;
        newLevel = newLevel > 7 ? 1 : newLevel;
        botUser.setRewardLevel(newLevel);
        return userRepository.save(botUser).getRewardLevel();
    }

    public int resetRewardLevel(BotUser botUser) {
        botUser.setRewardLevel(1);
        userRepository.save(botUser);
        return 1;
    }

    public void updateLastReward(BotUser botUser) {
        botUser.setLastReward(System.currentTimeMillis());
        userRepository.save(botUser);
    }

    public void resetEventPoints() {
        userRepository.resetEventPoints();
    }

    public BotUser increaseEventPoints(long userId) {
        BotUser botUser = getBotUser(userId);
        botUser.setEventPoints(botUser.getEventPoints() + 1);
        return userRepository.save(botUser);
    }
}
