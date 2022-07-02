package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.annotations.interactions.Button;
import com.github.kaktushose.jda.commands.dispatching.ButtonEvent;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.interactions.components.Buttons;
import de.kaktushose.levelbot.Levelbot;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

@CommandController(value = {"stop", "shutdown"}, ephemeral = true)
@Permission("owner")
public class StopCommand {

    @Inject
    private Levelbot levelbot;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Bot herunterfahren",
            usage = "{prefix}stop",
            desc = "Fährt den Bot herunter",
            category = "Owner"
    )
    public void onStop(CommandEvent event) {
        event.with(Buttons.enabled("onConfirm"), Buttons.enabled("onCancel"))
                .reply(embedCache.getEmbed("confirmAction").injectValue(
                        "action",
                        "du den Bot herunterfahren möchtest?"
                ));
    }

    @Button(label = "Abbrechen", style = ButtonStyle.DANGER)
    public void onCancel(ButtonEvent event) {
        event.clearComponents().edit(embedCache.getEmbed("interactionCancel"));
    }

    @Button(label = "Okay", style = ButtonStyle.SUCCESS)
    public void onConfirm(ButtonEvent event) {
        event.reply("https://tenor.com/view/tekashi-69-fade-out-peace-gif-15141419");
        levelbot.stop().terminate(0);
    }

}
