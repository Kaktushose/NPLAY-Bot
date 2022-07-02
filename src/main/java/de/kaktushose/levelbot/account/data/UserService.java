package de.kaktushose.levelbot.account.data;

import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
    }

    @Deprecated
    public List<BotUser> getAllUsers() {
        List<BotUser> result = new ArrayList<>();
        userRepository.findAll().forEach(result::add);
        return result;
    }

    @Deprecated
    public List<Long> getAllUserIds() {
        return userRepository.findAllIds();
    }

    public BotUser getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    /**
     *
     * @deprecated use {@link #isMuted(long)}
     */
    @Deprecated
    public List<Long> getMutedUsers() {
        return userRepository.findMutedUsers();
    }

    public boolean isMuted(long userId) {
        return false;
        // TODO implement
    }

    public List<Long> getUsersByPermission(int permissionLevel) {
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

    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    public void exchangeDiamonds(long userId, long diamonds) {
        BotUser botUser = getUserById(userId);
        botUser.setDiamonds(botUser.getDiamonds() - diamonds);
        botUser.setCoins(botUser.getCoins() + diamonds * 20);
        userRepository.save(botUser);
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

    public BotUser addCurrencies(long userId, long coins, long xp, long diamonds) {
        return addCurrencies(getUserById(userId), coins, xp, diamonds);
    }

    public BotUser addCurrencies(BotUser botUser, long coins, long xp, long diamonds) {
        return botUser;
        // TODO implement
    }

    public BotUser addReward(long userId, Reward reward) {
        return addReward(getUserById(userId), reward);
    }

    public BotUser addReward(BotUser botUser, Reward reward) {
        return botUser;
        // TODO implement
    }

    /**
     *
     * @deprecated {@link #addCurrencies(long, long, long, long)} {@link #addReward(long, Reward)}
     */
    @Deprecated
    public long addCoins(long userId, long amount) {
        BotUser botUser = getUserById(userId);
        botUser.setCoins(botUser.getCoins() + amount);
        userRepository.save(botUser);
        return botUser.getCoins();
    }

    /**
     *
     * @deprecated {@link #addCurrencies(long, long, long, long)} {@link #addReward(long, Reward)}
     */
    @Deprecated
    public long addXp(long userId, long amount) {
        BotUser botUser = getUserById(userId);
        botUser.setXp(botUser.getXp() + amount);
        userRepository.save(botUser);
        return botUser.getXp();
    }

    /**
     *
     * @deprecated {@link #addCurrencies(long, long, long, long)} {@link #addReward(long, Reward)}
     */
    @Deprecated
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


    public void onValidMessage(long userId) {
        // TODO implement
    }

    /**
     *
     * @deprecated {@link #onValidMessage(long)}
     */
    @Deprecated
    public void updateLastValidMessage(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setLastValidMessage(System.currentTimeMillis());
        userRepository.save(botUser);
    }

    /**
     *
     * @deprecated {@link #onValidMessage(long)}
     */
    @Deprecated
    public void updateMessageCount(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setMessageCount(botUser.getMessageCount() + 1);
        userRepository.save(botUser);
    }

    // TODO make external script
    @Deprecated
    public void updateUserStatistics(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setStartCoins(botUser.getCoins());
        botUser.setStartXp(botUser.getXp());
        botUser.setStartDiamonds(botUser.getDiamonds());
        userRepository.save(botUser);
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

    @Deprecated
    public void resetEventPoints(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setEventPoints(0);
        userRepository.save(botUser);
    }

    // TODO move to event service
    @Deprecated
    public long increaseEventPoints(long userId) {
        BotUser botUser = getUserById(userId);
        botUser.setEventPoints(botUser.getEventPoints() + 1);
        return userRepository.save(botUser).getEventPoints();
    }
}
