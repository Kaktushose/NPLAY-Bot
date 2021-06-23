package de.kaktushose.levelbot.database.services;

import com.github.kaktushose.jda.commands.api.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.NitroBooster;
import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.database.repositories.NitroBoosterRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class BoosterService {

    private final NitroBoosterRepository nitroBoosterRepository;
    private final Levelbot levelbot;
    private final UserService userService;
    private final SettingsService settingsService;

    public BoosterService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        nitroBoosterRepository = context.getBean(NitroBoosterRepository.class);
        this.userService = levelbot.getUserService();
        this.settingsService = levelbot.getSettingsService();
        this.levelbot = levelbot;
    }

    public void updateBoosterStatus(Guild guild, TextChannel botChannel, EmbedCache embedCache) {
        // iterate through all actual nitro boosters
        guild.findMembers(member -> member.getTimeBoosted() != null).onSuccess(boosterList -> {
            boosterList.forEach(member -> {
                long userId = member.getIdLong();

                // user is already registered as an active booster in db, skip this one
                if (isActiveNitroBooster(userId)) {
                    return;
                }

                // user is in db, must be a resumed booster
                if (isNitroBooster(userId)) {
                    changeNitroBoosterStatus(userId, true);
                    addMonthlyReward(userId);
                    userService.addUpItem(userId, 3, levelbot);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostResume")
                                    .injectValue("user", member.getEffectiveName())
                                    .toMessageEmbed()
                            )).queue();
                    return;
                }
                // else, user is not in db, must be a first time booster
                createNewNitroBooster(userId);
                addOneTimeReward(userId);
                userService.addUpItem(userId, 3, levelbot);
                botChannel.sendMessage(member.getAsMention())
                        .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostStart")
                                .injectValue("user", member.getEffectiveName())
                                .toMessageEmbed()
                        )).queue();
            });

            // iterate through all active boosters and compare with actual boosters
            getActiveNitroBoosters().forEach(nitroBooster -> {
                Long userId = nitroBooster.getUserId();
                Member member = guild.getMemberById(userId);

                if (boosterList.stream().map(ISnowflake::getIdLong).noneMatch(userId::equals)) {
                    changeNitroBoosterStatus(userId, false);
                    userService.removeItem(userId, 3, levelbot);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessage(embedCache.getEmbed("nitroBoostStop")
                                    .injectValue("user", member.getAsMention())
                                    .toMessageEmbed()
                            )).queue();
                }
            });
        }).onError(throwable -> {
            throw new IllegalStateException("Unable to query boosters!", throwable);
        });
    }

    public List<NitroBooster> getAllNitroBoosters() {
        List<NitroBooster> result = new ArrayList<>();
        nitroBoosterRepository.findAll().forEach(result::add);
        return result;
    }

    public List<NitroBooster> getActiveNitroBoosters() {
        return nitroBoosterRepository.getActiveNitroBoosters();
    }

    public boolean isNitroBooster(long userId) {
        return nitroBoosterRepository.findById(userId).isPresent();
    }

    public boolean isActiveNitroBooster(long userId) {
        return getActiveNitroBoosters().stream().map(NitroBooster::getUserId).anyMatch(((Long) userId)::equals);
    }

    public void createNewNitroBooster(long userId) {
        nitroBoosterRepository.save(new NitroBooster(userId, System.currentTimeMillis(), true));
    }

    public void changeNitroBoosterStatus(long userId, boolean active) {
        NitroBooster nitroBooster = nitroBoosterRepository.findById(userId).orElseThrow();
        nitroBooster.setActive(active);
        nitroBoosterRepository.save(nitroBooster);
    }

    public String addMonthlyReward(long userId) {
        Reward reward = settingsService.getMonthlyNitroBoosterReward();
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        if (reward.getItem() != null) {
            userService.addUpItem(userId, reward.getItem().getItemId(), levelbot);
        }
        return reward.getMessage();
    }

    public String addOneTimeReward(long userId) {
        Reward reward = settingsService.getOneTimeNitroBoosterReward();
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        if (reward.getItem() != null) {
            userService.addUpItem(userId, reward.getItem().getItemId(), levelbot);
        }
        return reward.getMessage();
    }
}
