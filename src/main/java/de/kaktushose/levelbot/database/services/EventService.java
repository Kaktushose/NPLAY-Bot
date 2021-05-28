package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.database.model.CurrencyChance;
import de.kaktushose.levelbot.database.repositories.ChancesRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

public class EventService {

    private final ChancesRepository chancesRepository;
    private final SettingsService settingsService;

    public EventService(SettingsService settingsService) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        this.chancesRepository = context.getBean(ChancesRepository.class);
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
}
