package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.components.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.modals.ModalEvent;
import com.github.kaktushose.jda.commands.dispatching.reply.Replyable;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.google.gson.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.List;
import java.util.Optional;

@Interaction
@Permissions(BotPermissions.MANAGE_EVENTS)
public class CollectRewardCommands {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;
    private static final Gson gson = new Gson();
    private static final String ROLE_REWARD = "Rolle";
    private static final String XP_REWARD = "XP";
    private String name;
    private String rewardType;
    private Role role;
    private int xp;
    private int threshold;
    private String embed;

    @SlashCommand(value = "collect reward create", desc = "Erstellt eine Belohnung für das Collect Event", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onRewardCreate(CommandEvent event, @Param("Der interne Name dieser Belohnung") String name, @Param("Der Wert, ab wann die Belohnung vergeben werden soll") int threshold) {
        this.name = name;
        this.threshold = threshold;
        event.withSelectMenus("onSelectType").reply(embedCache.getEmbed("rewardCreateSelectType").injectValue("name", name));
    }

    @StringSelectMenu("Wähle eine Belohnungsart aus")
    @SelectOption(label = "Rolle", value = ROLE_REWARD)
    @SelectOption(label = "XP", value = XP_REWARD)
    public void onSelectType(ComponentEvent event, List<String> selection) {
        rewardType = selection.get(0);
        if (ROLE_REWARD.equals(rewardType)) {
            event.withSelectMenus("onSelectRole").reply(embedCache.getEmbed("rewardCreateSelectRole").injectValue("name", name));
        } else if (XP_REWARD.equals(rewardType)) {
            event.replyModal("onSelectXp");
        } else {
            throw new IllegalArgumentException(String.format("%s ist keine gültige Auswahl!", rewardType));
        }
    }

    @EntitySelectMenu(value = net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE, placeholder = "Wähle eine Rolle aus")
    public void onSelectRole(ComponentEvent event, Mentions mentions) {
        role = mentions.getRoles().get(0);
        event.replyModal("onSelectEmbed");
    }

    @Modal("Embed angeben")
    public void onSelectEmbed(ModalEvent event, @TextInput(value = "Das Embed im JSON-Format", label = "Embed") String embed) {
        parseJson(embed).ifPresentOrElse(it -> {
            this.embed = it;
            finishSetup(event);
        }, () -> event.withButtons("onRetryEmbedInput").reply(embedCache.getEmbed("rewardCreateInvalidEmbed").injectValue("name", name)));
    }

    @Button("neue Eingabe")
    public void onRetryEmbedInput(ComponentEvent event) {
        event.replyModal("onSelectEmbed");
    }

    @Modal("XP-Belohnung")
    public void onSelectXp(ModalEvent event,
                           @TextInput(style = TextInputStyle.SHORT, label = "XP-Menge", value = "Eine Zahl zwischen 1 und 2.147.483.647") String amount,
                           @TextInput(value = "Das Embed im JSON-Format", label = "Embed") String embed) {
        int xp;
        try {
            xp = Integer.parseInt(amount);
        } catch (NumberFormatException ignored) {
            event.withButtons("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidXp").injectValue("name", name));
            return;
        }
        if (xp < 1) {
            event.withButtons("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidXp").injectValue("name", name));
            return;
        }
        parseJson(embed).ifPresentOrElse(it -> {
            this.embed = it;
            this.xp = xp;
            finishSetup(event);
        }, () -> event.withButtons("onRetryXpInput").reply(embedCache.getEmbed("rewardCreateInvalidEmbed").injectValue("name", name)));
    }

    @Button("neue Eingabe")
    public void onRetryXpInput(ComponentEvent event) {
        event.replyModal("onSelectXp");
    }

    private void finishSetup(Replyable event) {
        event.withButtons("onConfirm", "onCancel").reply(embedCache.getEmbed("rewardCreateSummarize")
                .injectValue("name", name)
                .injectValue("type", rewardType)
                .injectValue("threshold", threshold)
                .injectValue("reward", role == null ? String.valueOf(xp) : role.getAsMention())
        );
    }

    @Button(value = "Erstellen", style = ButtonStyle.SUCCESS)
    public void onConfirm(ComponentEvent event) {
        database.getCollectEventService().createCollectReward(name, threshold, xp, role, embed);
        event.reply(embedCache.getEmbed("rewardCreateConfirm").injectValue("name", name));
        event.removeComponents();
    }

    @Button(value = "Abbrechen", style = ButtonStyle.DANGER)
    public void onCancel(ComponentEvent event) {
        event.reply(embedCache.getEmbed("rewardCreateCancel"));
        event.removeComponents();
    }

    @SlashCommand(value = "collect reward delete", desc = "Löscht eine oder mehrere Belohnung(en) für das Collect Event", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onRewardDelete(CommandEvent event) {
        var rewards = database.getCollectEventService().getCollectRewards();

        var menu = ((net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu)
                event.getJdaCommands().getSelectMenu("CollectRewardCommands.onRewardDeleteSelect")).createCopy();
        menu.getOptions().clear();
        menu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT);
        rewards.forEach(it -> menu.addOption(it.name(), String.valueOf(it.rewardId())));
        event.getReplyContext().getBuilder().addActionRow(menu.build());
        event.reply(embedCache.getEmbed("rewardDeleteSelect"));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Belohnungen aus")
    @SelectOption(label = "dummy option", value = "dummy option")
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
            if (!(object.has("title") || object.has("description"))) {
                return Optional.of(object.toString());
            }
            return Optional.empty();
        } catch (JsonSyntaxException ignored) {
            return Optional.empty();
        }
    }
}
