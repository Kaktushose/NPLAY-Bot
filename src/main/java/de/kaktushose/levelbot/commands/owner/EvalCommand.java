package de.kaktushose.levelbot.commands.owner;

import com.github.kaktushose.jda.commands.annotations.*;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@CommandController(value = "eval", isActive = false)
@Permission("owner")
public class EvalCommand {

    private static final Logger log = LoggerFactory.getLogger(EvalCommand.class);
    private final ScriptEngine engine;
    @Inject
    private Levelbot levelbot;
    @Inject
    private EmbedCache embedCache;

    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util)"
            );
        } catch (ScriptException e) {
            log.error("Unable to initialize script engine!", e);
        }
    }

    @Command(
            name = "Code ausführen",
            usage = "{prefix}eval <code>",
            desc = "Führt Code in der aktuellen Runtime des Bots aus",
            category = "Owner"
    )
    public void onEval(CommandEvent event, @Concat String code) {
        log.warn("Executing eval command with code: {}", code);
        engine.put("levelbot", levelbot);
        engine.put("event", event);
        code = code.replaceAll("`", "");

        String script = String.format("(function() { with (imports) {%s} } )();", code);
        Object result;
        String color;
        try {
            result = engine.eval(script);
            color = "#86c240";
        } catch (ScriptException e) {
            result = e;
            color = "#aa0c14";
        }
        log.info("Eval Command returned result: {}", result);
        event.reply(embedCache.getEmbed("evalCommand")
                .injectValue("result", result)
                .injectValue("color", color)
        );
    }
}
