package de.kaktushose.levelbot.database.services;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.UserService;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.NitroBooster;
import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.database.repositories.NitroBoosterRepository;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.ItemCategory;
import de.kaktushose.levelbot.shop.data.items.ItemVariant;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoosterService {

    private static final Logger log = LoggerFactory.getLogger("analytics");
    private final NitroBoosterRepository nitroBoosterRepository;
    private final Levelbot levelbot;
    private final ShopService shopService;
    private final UserService userService;
    private final SettingsService settingsService;

    public BoosterService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        nitroBoosterRepository = context.getBean(NitroBoosterRepository.class);
        this.shopService = levelbot.getShopService();
        this.settingsService = levelbot.getSettingsService();
        userService = levelbot.getUserService();
        this.levelbot = levelbot;
    }

    public void updateBoosterStatus(Guild guild, TextChannel botChannel, EmbedCache embedCache) {
        // iterate through all actual nitro boosters
        log.debug("updateBoosterStatus started:");
        guild.findMembers(member -> member.getTimeBoosted() != null).onSuccess(boosterList -> {
            log.debug("Queried members: {}", Arrays.toString(boosterList.toArray()));
            boosterList.forEach(member -> {
                long userId = member.getIdLong();
                log.debug("Checking on {}...", member);
                // user is already registered as an active booster in db, skip this one
                if (isActiveNitroBooster(userId)) {
                    log.debug("Member is active booster!");
                    return;
                }

                // user is in db, must be a resumed booster
                if (isNitroBooster(userId)) {
                    changeNitroBoosterStatus(userId, true);
                    addMonthlyReward(userId);
                    shopService.addItem(userId, ItemCategory.PREMIUM, ItemVariant.UNLIMITED);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessageEmbeds(embedCache.getEmbed("nitroBoostResume")
                                    .injectValue("user", member.getEffectiveName())
                                    .toMessageEmbed()
                            )).queue();
                    log.debug("Member is inactive booster!");
                    return;
                }
                // else, user is not in db, must be a first time booster
                createNewNitroBooster(userId);
                addOneTimeReward(userId);
                shopService.addItem(userId, ItemCategory.PREMIUM, ItemVariant.UNLIMITED);
                botChannel.sendMessage(member.getAsMention())
                        .and(botChannel.sendMessageEmbeds(embedCache.getEmbed("nitroBoostStart")
                                .injectValue("user", member.getEffectiveName())
                                .toMessageEmbed()
                        )).queue();
                log.debug("Member is new booster!");
            });

            log.debug("Comparing with active boosters...");
            // iterate through all active boosters and compare with actual boosters
            getActiveNitroBoosters().forEach(nitroBooster -> {
                Long userId = nitroBooster.getUserId();
                Member member = guild.retrieveMemberById(userId).complete();
                log.debug("Checking on {}...", member);
                if (boosterList.stream().map(ISnowflake::getIdLong).noneMatch(userId::equals)) {
                    log.debug("Member stopped boosting!");
                    changeNitroBoosterStatus(userId, false);
                    shopService.removeItem(userId, ItemCategory.PREMIUM, ItemVariant.UNLIMITED);
                    botChannel.sendMessage(member.getAsMention())
                            .and(botChannel.sendMessageEmbeds(embedCache.getEmbed("nitroBoostStop")
                                    .injectValue("user", member.getAsMention())
                                    .toMessageEmbed()
                            )).queue();
                }
                log.debug("Member is still boosting!");
            });
        }).onError(throwable -> {
            log.debug("Querying members failed!", throwable);
            throw new IllegalStateException("Unable to query boosters!", throwable);
        });
        log.debug("updateBoosterStatus finished!");
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
        if (!isNitroBooster(userId)) {
            return;
        }
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
            shopService.addItem(userId, reward.getItem().getItemId());
        }
        return reward.getMessage();
    }

    public String addOneTimeReward(long userId) {
        Reward reward = settingsService.getOneTimeNitroBoosterReward();
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        if (reward.getItem() != null) {
            shopService.addItem(userId, reward.getItem().getItemId());
        }
        return reward.getMessage();
    }
}
