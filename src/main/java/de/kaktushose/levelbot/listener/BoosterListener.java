package de.kaktushose.levelbot.listener;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoosterListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger("analytics");

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        log.debug("onGuildMemberUpdateBoostTime:\nNewTimeBoosted: {}\nOldTimeBoosted: {}\nMember:{}\n",
                event.getNewTimeBoosted(),
                event.getOldTimeBoosted(),
                event.getMember());
    }

    @Override
    public void onGuildUpdateBoostTier(@NotNull GuildUpdateBoostTierEvent event) {
        log.debug("onGuildUpdateBoostTier\nNewBoostTier: {}\nOldBoostTier: {}\n",
                event.getNewBoostTier(),
                event.getOldBoostTier());
    }

    @Override
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {
        log.debug("onGuildUpdateBoostCount\nNewBoostCount: {}\nOldBoostCount: {}\n",
                event.getNewBoostCount(),
                event.getOldBoostCount());
    }
}
