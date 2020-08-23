package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class EventItem extends Item {

    private long roleId;

    public EventItem(int price, String name, String description, int type, long roleId) {
        super(price, name, description, 0, type, ItemType.EVENT);
        this.roleId = roleId;
    }

    public boolean validate(Bot bot, Member member) {
        Role role = bot.getJda().getRoleById(roleId);
        if (member.getRoles().contains(role)) {
            error = "Du besitzt dieses Item bereits!";
            return false;
        }
        return validateTransaction(bot.getDatabase().getBotUser(member.getIdLong()));
    }

    @Override
    public boolean validateTransaction(BotUser botUser) {
        if (botUser.getCoins() < price) {
            error = "Du hast nicht genügend Münzen!";
            return false;
        }
        return true;
    }

    @Override
    public void buy(Bot bot, Member member) {
        Role role = bot.getJda().getRoleById(roleId);
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        botUser.setCoins(botUser.getCoins() - price);
        bot.getDatabase().setBotUser(botUser);
        bot.addDiscordRole(member, role);
    }

    @Override
    public String getSuccessMessage() {
        return "Du besitzt jetzt " + name + "!";
    }

    @Override
    public void onItemExpiration(Bot bot, Member member) {
        throw new UnsupportedOperationException("Event items can't be removed");
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }
}
