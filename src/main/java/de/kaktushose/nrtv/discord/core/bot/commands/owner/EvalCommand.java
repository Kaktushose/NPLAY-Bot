package de.kaktushose.nrtv.discord.core.bot.commands.owner;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.Permissions;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;

@Permissions(PermissionLevel.BOTOWNER)
public class EvalCommand extends Command {

    private final Bot bot;
    private final ScriptEngine engine;

    public EvalCommand(Bot bot) {
        this.bot = bot;
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util)");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        try {
            engine.put("bot", bot);
            engine.put("logger", Logging.getLogger());
            engine.put("executor", executor);
            engine.put("arguments", args);
            engine.put("channel", channel);
            engine.put("message", message);
            engine.put("builder", new EmbedBuilder());
            engine.put("green", Color.GREEN);
            engine.put("red", Color.RED);
            engine.put("orange", Color.ORANGE);
            String script ="(function() {" +
                    "with (imports) {" +
                    message.getContentDisplay().substring(6).replaceAll("`", "") +
                    "}" +
                    "})();";
            Object out = engine.eval(script);

            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Erfolg")
                    .setDescription("```" + (out == null ? "Executed without error." : out.toString()) + "```").build()).queue();
        } catch (Exception e) {
            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("```" + e + "```").build()).queue();
        }
    }
}
