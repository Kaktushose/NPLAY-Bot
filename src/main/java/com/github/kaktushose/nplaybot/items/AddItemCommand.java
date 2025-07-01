package com.github.kaktushose.nplaybot.items;

import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.kaktushose.nplaybot.items.ItemService.PREMIUM_BASE_TYPE_ID;
import static com.github.kaktushose.nplaybot.items.ItemService.PREMIUM_UNLIMITED_ITEM_ID;

@Interaction
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
public class AddItemCommand {

    private static final Logger log = LoggerFactory.getLogger(AddItemCommand.class);
    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;
    private Member target;

    @Command(value = "balance item add", desc = "Fügt einem Nutzer ein Item hinzu")
    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    public void onItemAdd(CommandEvent event, Member target) {
        var items = database.getItemService().getAllItems();

        if (items.isEmpty()) {
            event.reply(embedCache.getEmbed("noOptions").injectValue("type", "Items"));
            return;
        }

        var transactions = database.getItemService().getTransactions(target);
        items = items.stream().filter(item -> {
            if (item.typeId() == PREMIUM_BASE_TYPE_ID) {
                return !transactions.stream().map(ItemService.Transaction::itemId).toList().contains(PREMIUM_UNLIMITED_ITEM_ID);
            }
            return !transactions.stream().map(ItemService.Transaction::typeId).toList().contains(item.typeId());
        }).toList();

        if (items.isEmpty()) {
            event.reply(embedCache.getEmbed("allItemsError"));
            return;
        }

        this.target = target;

        List<SelectOption> options = items.stream()
                .map(it -> SelectOption.of(it.name(), String.valueOf(it.itemId())))
                .toList();
        event.with()
                .components(Component.stringSelect("onItemAddSelect").selectOptions(options))
                .reply(embedCache.getEmbed("itemAddSelect"));
    }

    @StringSelectMenu("Wähle ein Item aus")
    public void onItemAddSelect(ComponentEvent event, List<String> selection) {
        selection.forEach(id -> database.getItemService().createTransaction(target, Integer.parseInt(id), "Add Item Command").ifPresent(role -> {
            log.info("Adding role {} to member {}", target, role);
            event.getGuild().addRoleToMember(target, role).queue();
        }));
        event.reply(embedCache.getEmbed("itemAdd"));
        event.removeComponents();
    }
}
