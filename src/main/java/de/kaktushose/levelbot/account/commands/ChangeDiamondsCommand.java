package de.kaktushose.levelbot.account.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.annotations.interactions.Button;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.data.StateSection;
import com.github.kaktushose.jda.commands.dispatching.ButtonEvent;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.interactions.components.Buttons;
import de.kaktushose.levelbot.account.data.BotUser;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

@CommandController(value = {"tauschen", "wechseln"}, category = "Levelsystem", ephemeral = true)
public class ChangeDiamondsCommand {

    private final StateSection stateSection;
    @Inject
    private EmbedCache embedCache;
    @Inject
    private UserService userService;

    public ChangeDiamondsCommand() {
        stateSection = new StateSection(1, TimeUnit.MINUTES);
    }

    @Command(
            name = "Diamanten tauschen",
            desc = "Tauscht Diamanten gegen Münzen ein. Ein Diamant ist 20 Münzen wert"
    )
    public void onChangeDiamonds(CommandEvent event, @Optional("1") @Param(value = "Die Anzahl an Diamanten", name = "anzahl") long amount) {
        BotUser botUser = userService.getBotUser(event.getAuthor());
        long diamonds = botUser.getDiamonds();

        if (diamonds == 0 || amount > diamonds) {
            event.reply(embedCache.getEmbed("missingCurrency").injectValue("currency", "Diamanten"));
            return;
        }

        event.withButtons("onConfirm", "onCancel").reply(embedCache.getEmbed("changeDiamonds")
                .injectFormat(amount == 1 ? "einen" : String.valueOf(amount), amount * UserService.EXCHANGE_RATE)
        );

        stateSection.section(event).put("user", botUser);
        stateSection.section(event).put("amount", amount);
    }

    @Button(label = "Abbrechen", style = ButtonStyle.DANGER)
    public void onCancel(ButtonEvent event) {
        event.clearComponents().edit(embedCache.getEmbed("interactionCancel"));
    }

    @Button(label = "Okay", style = ButtonStyle.SUCCESS)
    public void onConfirm(ButtonEvent event) {
        StateSection section = stateSection.section(event);
        java.util.Optional<BotUser> botUser = section.get("user", BotUser.class);
        java.util.Optional<Long> amount = section.get("amount", Long.class);

        if (botUser.isEmpty() || amount.isEmpty()) {
            event.clearComponents().edit(embedCache.getEmbed("interactionTimeout"));
            return;
        }

        userService.exchangeDiamonds(botUser.get(), amount.get());

        event.clearComponents().edit(
                embedCache.getEmbed("diamondChangeSuccess")
                        .injectValue("diamonds", amount.get() == 1 ? "einen" : amount.get())
                        .injectValue("coins", amount.get() * UserService.EXCHANGE_RATE)
        );
    }

}
