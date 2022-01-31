package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.entities.Member;

@CommandController({"info", "rank", "konto"})
public class RankInfoCommand {

    @Inject
    Levelbot levelbot;

    @Command(
            name = "Kontoinformation abrufen",
            usage = "{prefix}info <member>",
            desc = "Zeigt die Kontoinformationen zu einem User an",
            category = "Levelsystem"
    )
    public void onRankInfo(CommandEvent event, @Optional Member member) {
        Member target = member == null ? event.getMember() : member;
        event.reply(levelbot.generateRankInfo(target.getUser(), false));
    }
}

