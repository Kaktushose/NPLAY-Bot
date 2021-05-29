package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.database.model.ContestEntry;
import de.kaktushose.levelbot.database.model.CurrencyChance;
import de.kaktushose.levelbot.database.repositories.ChancesRepository;
import de.kaktushose.levelbot.database.repositories.ContestRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import de.kaktushose.levelbot.util.Pagination;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

public class EventService {

    private final ChancesRepository chancesRepository;
    private final ContestRepository contestRepository;
    private final SettingsService settingsService;

    public EventService(SettingsService settingsService) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        this.chancesRepository = context.getBean(ChancesRepository.class);
        this.contestRepository = context.getBean(ContestRepository.class);
        this.settingsService = settingsService;
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
}
