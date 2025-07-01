package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.List;
import java.util.Optional;

@Interaction
@Permissions(BotPermissions.MANAGE_EVENTS)
public class CollectRewardCommands {

    private static final Gson gson = new Gson();
    private static final String ROLE_REWARD = "Rolle";
    private static final String XP_REWARD = "XP";
    private static final String ITEM_REWARD = "Item";
    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;
    private String name;
    private String rewardType;
    private Role role;
    private ItemService.Item item;
    private int xp;
    private int threshold;
    private String embed;

    @Command(value = "events collect-reward create", desc = "Erstellt eine Belohnung für das Collect Event")
    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    public void onRewardCreate(CommandEvent event, @Param("Der interne Name dieser Belohnung") String name, @Param("Der Wert, ab wann die Belohnung vergeben werden soll") int threshold) {
        this.name = name;
        this.threshold = threshold;
        event.with().components("onSelectType").reply(embedCache.getEmbed("rewardCreateSelectType").injectValue("name", name));
    }

    @StringSelectMenu("Wähle eine Belohnungsart aus")
    @MenuOption(label = "Rolle", value = ROLE_REWARD)
    @MenuOption(label = "XP", value = XP_REWARD)
    @MenuOption(label = "Item", value = ITEM_REWARD)
    public void onSelectType(ComponentEvent event, List<String> selection) {
        rewardType = selection.get(0);
        if (ROLE_REWARD.equals(rewardType)) {
            event.with().components("onSelectRole").reply(embedCache.getEmbed("rewardCreateSelectRole").injectValue("name", name));
        } else if (XP_REWARD.equals(rewardType)) {
            event.replyModal("onSelectXp");
        } else if (ITEM_REWARD.equals(rewardType)) {
            var items = database.getItemService().getAllItems();
            if (items.isEmpty()) {
                event.reply(embedCache.getEmbed("noOptions").injectValue("type", "Items"));
                return;
            }
            List<SelectOption> options = items.stream()
                    .map(it -> SelectOption.of(it.name(), String.valueOf(it.itemId())))
                    .toList();
            event.with()
                    .components(Component.stringSelect("onSelectItem").selectOptions(options))
                    .reply(embedCache.getEmbed("rewardCreateSelectItem"));
        } else {
            throw new IllegalArgumentException(String.format("%s ist keine gültige Auswahl!", rewardType));
        }
    }

    @EntitySelectMenu(value = net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE, placeholder = "Wähle eine Rolle aus")
    public void onSelectRole(ComponentEvent event, Mentions mentions) {
        role = mentions.getRoles().get(0);
        event.replyModal("onSelectEmbed");
    }

    @StringSelectMenu("Wähle ein Item aus")
    public void onSelectItem(ComponentEvent event, List<String> selection) {
        var itemId = selection.stream().findFirst().orElseThrow();
        item = database.getItemService().getItem(Integer.parseInt(itemId));
        event.replyModal("onSelectEmbed");
    }

    @Modal("Embed angeben")
    public void onSelectEmbed(ModalEvent event, @TextInput(placeholder = "Das Embed im JSON-Format", value = "Embed") String embed) {
        parseJson(embed).ifPresentOrElse(it -> {
            this.embed = it;
            finishSetup(event);
        }, () -> event.with().components("onRetryEmbedInput").reply(embedCache.getEmbed("rewardCreateInvalidEmbed").injectValue("name", name)));
    }

    @Button("neue Eingabe")
    public void onRetryEmbedInput(ComponentEvent event) {
        event.replyModal("onSelectEmbed");
    }

    @Modal("XP-Belohnung")
    public void onSelectXp(ModalEvent event,
                           @TextInput(style = TextInputStyle.SHORT, value = "XP-Menge", placeholder = "Eine Zahl zwischen 1 und 2.147.483.647") String amount,
                           @TextInput(placeholder = "Das Embed im JSON-Format", value = "Embed") String embed) {
        int xp;
        try {
            xp = Integer.parseInt(amount);
        } catch (NumberFormatException ignored) {
            event.with().components("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidXp").injectValue("name", name));
            return;
        }
        if (xp < 1) {
            event.with().components("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidXp").injectValue("name", name));
            return;
        }
        parseJson(embed).ifPresentOrElse(it -> {
            this.embed = it;
            this.xp = xp;
            finishSetup(event);
        }, () -> event.with().components("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidEmbed").injectValue("name", name)));
    }

    @Button("neue Eingabe")
    public void onRetryXpInput(ComponentEvent event) {
        event.replyModal("onSelectXp");
    }

    private void finishSetup(ModalEvent event) {
        String reward;
        if (role != null) {
            reward = role.getAsMention();
        } else if (item != null) {
            reward = item.name();
        } else {
            reward = String.valueOf(xp);
        }
        event.with().components("onConfirm", "onCancel").reply(embedCache.getEmbed("rewardCreateSummarize")
                .injectValue("name", name)
                .injectValue("type", rewardType)
                .injectValue("threshold", threshold)
                .injectValue("reward", reward)
        );
    }

    @Button(value = "Erstellen", style = ButtonStyle.SUCCESS)
    public void onConfirm(ComponentEvent event) {
        database.getCollectEventService().createCollectReward(name, threshold, xp, role, item, embed);
        event.reply(embedCache.getEmbed("rewardCreateConfirm").injectValue("name", name));
        event.removeComponents();
    }

    @Button(value = "Abbrechen", style = ButtonStyle.DANGER)
    public void onCancel(ComponentEvent event) {
        event.reply(embedCache.getEmbed("rewardCreateCancel"));
        event.removeComponents();
    }

    @Command(value = "events collect-reward delete", desc = "Löscht eine oder mehrere Belohnung(en) für das Collect Event")
    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    public void onRewardDelete(CommandEvent event) {
        var rewards = database.getCollectEventService().getCollectRewards();

        if (rewards.isEmpty()) {
            event.reply(embedCache.getEmbed("noOptions").injectValue("type", "Belohnungen"));
            return;
        }
        List<SelectOption> options = rewards.stream()
                .map(it -> SelectOption.of(it.name(), String.valueOf(it.rewardId())))
                .toList();
        event.with()
                .components(Component.stringSelect("onRewardDeleteSelect").selectOptions(options))
                .reply(embedCache.getEmbed("rewardDeleteSelect"));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Belohnungen aus")
    public void onRewardDeleteSelect(ComponentEvent event, List<String> selection) {
        for (var id : selection) {
            database.getCollectEventService().deleteCollectReward(Integer.parseInt(id));
        }
        event.reply(embedCache.getEmbed("rewardDelete"));
    }

    private Optional<String> parseJson(String json) {
        try {
            JsonObject object = gson.fromJson(json, JsonObject.class);
            if (object.has("embeds")) {
                if (!object.get("embeds").isJsonArray()) {
                    return Optional.empty();
                }
                object = object.get("embeds").getAsJsonArray().get(0).getAsJsonObject();
            }
            if (object.has("title") || object.has("description")) {
                return Optional.of(object.toString());
            }
            return Optional.empty();
        } catch (JsonSyntaxException ignored) {
            return Optional.empty();
        }
    }
}
