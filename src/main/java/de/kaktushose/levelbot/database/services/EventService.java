package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.database.model.CollectEvent;
import de.kaktushose.levelbot.database.model.ContestEntry;
import de.kaktushose.levelbot.database.model.CurrencyChance;
import de.kaktushose.levelbot.database.repositories.ChancesRepository;
import de.kaktushose.levelbot.database.repositories.CollectEventRepository;
import de.kaktushose.levelbot.database.repositories.ContestRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventService {

    private final ChancesRepository chancesRepository;
    private final ContestRepository contestRepository;
    private final CollectEventRepository collectEventRepository;
    private final SettingsService settingsService;
    private final UserService userService;

    public EventService(SettingsService settingsService, UserService userService) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        this.chancesRepository = context.getBean(ChancesRepository.class);
        this.contestRepository = context.getBean(ContestRepository.class);
        this.collectEventRepository = context.getBean(CollectEventRepository.class);
        this.settingsService = settingsService;
        this.userService = userService;
    }

    public String startBalanceEvent(int eventId, long guildId) {
        switch (eventId) {
            case 0:
                for (CurrencyChance chance : chancesRepository.getXpChances()) {
                    chance.setAmount(chance.getAmount() * 2);
                    chancesRepository.save(chance);
                }
                return "XP-Booster x2";
            case 1:
                for (CurrencyChance chance : chancesRepository.getCoinChances()) {
                    chance.setAmount(chance.getAmount() * 2);
                    chancesRepository.save(chance);
                }
                return "Münzen-Rush x2";
            case 2:
                for (CurrencyChance chance : chancesRepository.getDiamondChances()) {
                    if (chance.getAmount() == 0) {
                        chance.setChance(80);
                        chancesRepository.save(chance);
                    }
                }
                return "Diamanten-Regen x2";
            case 3:
                settingsService.setMessageCooldown(guildId, TimeUnit.MINUTES.toMillis(5));
                return "Cooldown -67%";
            default:
                return "N/A";
        }
    }

    public String stopBalanceEvent(int eventId, long guildId) {
        switch (eventId) {
            case 0:
                for (CurrencyChance chance : chancesRepository.getXpChances()) {
                    chance.setAmount(chance.getAmount() / 2);
                    chancesRepository.save(chance);
                }
                return "XP-Booster x2";
            case 1:
                for (CurrencyChance chance : chancesRepository.getCoinChances()) {
                    chance.setAmount(chance.getAmount() / 2);
                    chancesRepository.save(chance);
                }
                return "Münzen-Rush x2";
            case 2:
                for (CurrencyChance chance : chancesRepository.getDiamondChances()) {
                    if (chance.getAmount() == 0) {
                        chance.setChance(90);
                        chancesRepository.save(chance);
                    }
                }
                return "Diamanten-Regen x2";
            case 3:
                settingsService.setMessageCooldown(guildId, TimeUnit.MINUTES.toMillis(15));
                return "Cooldown -67%";
            default:
                return "N/A";
        }
    }

    public void startContestEvent(long guildId, long channelId, String emote) {
        settingsService.setEventChannelId(guildId, channelId);
        settingsService.setEventEmote(guildId, emote.replaceAll(":", ""));
        contestRepository.deleteAll();
    }

    public void stopContestEvent(long guildId) {
        settingsService.setEventChannelId(guildId, 0);
        settingsService.setEventEmote(guildId, "");
    }

    public boolean voteCountExists(long userId) {
        return contestRepository.existsByUserId(userId);
    }

    public void increaseVoteCount(long messageId) {
        ContestEntry entry = contestRepository.findById(messageId).orElseThrow();
        entry.setCount(entry.getCount(Pagination.CurrencyType.CONTEST) + 1);
        contestRepository.save(entry);
    }

    public void decreaseVoteCount(long messageId) {
        ContestEntry entry = contestRepository.findById(messageId).orElseThrow();
        entry.setCount(entry.getCount(Pagination.CurrencyType.CONTEST) - 1);
        contestRepository.save(entry);
    }

    public void createVoteCount(long messageId, long userId) {
        contestRepository.save(new ContestEntry(messageId, userId, 0));
    }

    public void deleteVoteCount(long messageId) {
        if (!contestRepository.existsById(messageId)) {
            return;
        }
        contestRepository.deleteById(messageId);
    }

    public Pagination getVoteResult(int pageSize, JDA jda) {
        return new Pagination(pageSize, contestRepository.getContestResult(), jda, Pagination.CurrencyType.CONTEST);
    }

    public boolean contestEventExistsById(int id) {
        return collectEventRepository.existsById(id);
    }

    public boolean collectEventExistsById(int id) {
        return collectEventRepository.existsById(id);
    }

    public CollectEvent getCollectEvent(int id) {
        return collectEventRepository.findById(id).orElseThrow();
    }

    public List<CollectEvent> getAllCollectEvents() {
        List<CollectEvent> result = new ArrayList<>();
        collectEventRepository.findAll().forEach(result::add);
        return result;
    }

    public String startCollectEvent(int id, long guildId) {
        settingsService.setActiveCollectEvent(guildId, id);
        userService.getAllUsers().forEach(botUser -> userService.resetEventPoints(botUser.getUserId()));
        return getCollectEvent(id).getName();
    }

    public boolean stopCollectEvent(long guildId) {
        if (!isCollectEventActive(guildId)) {
            return false;
        }
        settingsService.setActiveCollectEvent(guildId, -1);
        return true;
    }

    public CollectEvent getActiveCollectEvent(long guildId) {
        return getCollectEvent(settingsService.getActiveCollectEventId(guildId));
    }

    public boolean isCollectEventActive(long guildId) {
        return settingsService.getActiveCollectEventId(guildId) > -1;
    }
}
