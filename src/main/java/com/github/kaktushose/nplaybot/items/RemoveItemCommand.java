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
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;

@Interaction
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class RemoveItemCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;
    private Member target;

    @Command(value = "balance item remove", desc = "Entfernt einem Nutzer ein oder mehrere Items")
    public void onItemRemove(CommandEvent event, Member target) {
        var transactions = database.getItemService().getTransactions(target);

        if (transactions.isEmpty()) {
            event.reply(embedCache.getEmbed("noOptions").injectValue("type", "Items"));
            return;
        }

        this.target = target;

        List<SelectOption> options = transactions.stream()
                .map(it -> SelectOption.of(it.name(), String.valueOf(it.transactionId())))
                .toList();
        event.with()
                .components(Component.stringSelect("onItemRemoveSelect").selectOptions(options))
                .reply(embedCache.getEmbed("itemRemoveSelect"));
    }

    @StringSelectMenu("Wähle ein oder mehrere Items aus")
    public void onItemRemoveSelect(ComponentEvent event, List<String> selection) {
        selection.forEach(id -> database.getItemService().deleteTransaction(target, Integer.parseInt(id)));
        event.reply(embedCache.getEmbed("itemDelete"));
        event.removeComponents();
    }
}
