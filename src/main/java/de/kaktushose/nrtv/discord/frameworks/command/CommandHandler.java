package de.kaktushose.nrtv.discord.frameworks.command;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;


public class CommandHandler extends ListenerAdapter {

    private final Bot bot;
    private final Logger logger;
    private final Map<String, Command> commands;
    private final Map<String[], Command> aliasCommands;
    private HelpCommand helpCommand;
    private String helpInvoke;

    public CommandHandler(Bot bot) {
        this.bot = bot;
        logger = Logging.getLogger();
        commands = new HashMap<>();
        aliasCommands = new HashMap<>();
    }

    public CommandHandler addCommand(Command command, String invoke, String... alias) {
        commands.put(invoke, command);
        aliasCommands.put(alias, command);
        return this;
    }


    public CommandHandler setHelpCommand(HelpCommand command, String invoke) {
        this.helpCommand = command;
        this.helpInvoke = invoke;
        return this;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith(bot.getPrefix())) return;
        if (!bot.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
            event.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Fehler")
                    .setColor(Color.RED)
                    .setDescription("unzureichende Berechtigungen")
                    .build()).queue();
            return;
        }
        handle(event);
    }

    private void handle(GuildMessageReceivedEvent event) {
        String[] raw = parse(event.getMessage().getContentRaw());
        String invoke = raw[0].toLowerCase();
        String[] args = Arrays.copyOfRange(raw, 1, raw.length);

        if (invoke.equals(helpInvoke)) {
            logger.debug(helpInvoke + " got triggered with following arguments: " + Arrays.toString(args));
            if (args.length < 1) {
                logger.debug("executing " + helpCommand.getClass().getSimpleName());
                helpCommand.onCommand(event.getMember(), args, event.getChannel(), event.getMessage());
            } else {
                commands.keySet().forEach(s -> {
                    if (s.equals(args[0])) {
                        logger.info("executing help command for " + helpCommand.getClass().getSimpleName() + " user " + event.getAuthor().getId());
                        commands.get(s).onHelp(event.getMember(), event.getChannel());
                    }
                });
            }
            logger.debug("no suitable help found");
            return;
        }

        Command command = null;

        if (commands.containsKey(invoke)) {
            command = commands.get(invoke);
        } else {
            boolean noMatch = true;
            for (String[] alias : aliasCommands.keySet()) {
                for (String s : alias) {
                    if (invoke.equals(s)) {
                        command = aliasCommands.get(alias);
                        noMatch = false;
                        break;
                    }
                }
            } if (noMatch) {
                logger.debug("No command found for invoke: " + invoke);
                return;
            }
        }

        Class<?> commandClass = command.getClass();
        Arguments arguments = parseArgs(args);

        if (commandClass.isAnnotationPresent(Permissions.class)) {
            Permissions permission = (Permissions) commandClass.getAnnotation(Permissions.class);
            if (!bot.hasPermission(event.getMember(), permission.value())) {
                command.onInsufficientPermissions(event.getMember(), event.getChannel());
                return;
            }
        }

        boolean executed = false;
        for (Method method : commandClass.getDeclaredMethods()) {
            boolean isMatching = true;

            if (method.getModifiers() != Modifier.PUBLIC) {
                continue;
            }

            if (!method.isAnnotationPresent(SubCommand.class)) {
                continue;
            }
            if (args.length < 1) {
                break;
            }

            SubCommand subCommand = method.getAnnotation(SubCommand.class);
            String[] values = subCommand.value();
            logger.debug("checking parameter for: " + method.getName() + " from " + method.getDeclaringClass().getSimpleName());
            for (int i = 0; i < values.length; i++) {

                if (!isMatching) {
                    break;
                }
                logger.debug("parameter value " + values[i] + " at position " + (i + 1) + " of " + values.length);
                switch (values[i]) {
                    case "@Member":
                        isMatching = arguments.isMember(i);
                        logger.debug("@Member matching: " + isMatching);
                        break;
                    case "#Channel":
                        isMatching = arguments.isChannel(i);
                        logger.debug("#Channel matching: " + isMatching);
                        break;
                    case "@Rolle":
                        //funktioniert nur, wenn Rollen ID angegeben wird, wird im Moment aber auch nicht benötigt
                        isMatching = isRole(args[i]);
                        logger.debug("@Rolle matching: " + isMatching);
                        break;
                    case "<@Member|@Rolle|all>":
                        isMatching = (arguments.isMember(i) || arguments.isMemberList(i) || isRole(args[i]));
                        logger.debug("<@Member|@Rolle|all> matching: " + isMatching);
                        break;
                    case "Zahl":
                        isMatching = arguments.isInteger(i);
                        logger.debug("Zahl matching: " + isMatching);
                        break;
                    default:
                        isMatching = values[i].equals(args[i]);
                        break;
                }
            }

            if (isMatching) {
                try {
                    method.invoke(command, event.getMember(), arguments, event.getChannel(), event.getMessage());
                    logger.info("executing sub command " + method.getName() + " for " + method.getDeclaringClass().getSimpleName() + " user " + event.getAuthor().getId());
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.info("an error has occurred while executing " + method.getName() + " from " + method.getDeclaringClass().getSimpleName(), e);
                    event.getChannel().sendMessage(new EmbedBuilder()
                            .setTitle("Command Ausführung fehlgeschlagen")
                            .setDescription("Da ist etwas schiefgelaufen! Bitte kontaktiere <@393843637437464588>")
                            .addField("Details:", e.toString(), false)
                            .setColor(Color.RED)
                            .setFooter(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()))
                            .build()).queue();
                }
                executed = true;
            }
        }

        if (!executed) {
            command.onCommand(event.getMember(), arguments, event.getChannel(), event.getMessage());
            logger.info("executing default command for " + command.getClass().getSimpleName() + " user " + event.getAuthor().getId());
        }
    }

    private String[] parse(String content) {
        logger.debug("command parser got input: " + content);
        content = content.replaceFirst(bot.getPrefix(), "");
        while (content.contains("  ")) {
            content = content.replaceAll(" {2}", " ");
        }
        return content.split(" ");
    }

    private Arguments parseArgs(String[] args) {
        Arguments arguments = new Arguments();
        Guild guild = bot.getGuild();
        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            if (s.isEmpty()) {
                continue;
            }

            logger.debug("args parsing got input: >>>" + s + "<<<");

            if (s.charAt(s.length() - 1) == '\"') {
                s = s.replaceAll("\"", "");
            }

            if (s.startsWith("\"")) {
                int j = i + 1;
                boolean valid = true;
                StringBuilder builder = new StringBuilder(s);
                do {
                    builder.append(" ");
                    builder.append(args[j]);
                    if (args[j].contains("\"")) {
                        valid = false;

                    }
                    args[j] = "";
                    j++;
                }
                while (valid);
                s = builder.toString().replaceAll("\"", "");
                args[i] = s;
                parseArgs(args);
            }

            if (s.matches("<[@#][&!]?([0-9]{4,})>")) {
                s = s.replace("<", "")
                        .replace(">", "")
                        .replace("@", "")
                        .replace("&", "")
                        .replace("#", "")
                        .replace("!", "");
            }
            logger.debug("parsed to: " + s);

            Member member = null;
            try {
                member = guild.getMemberById(s);
            } catch (NumberFormatException ignored) {
                logger.debug("no member found with id: " + s);
                logger.debug("checking for nicknames");
                List<Member> memberList = guild.getMembersByEffectiveName(s, true);
                if (!memberList.isEmpty()) {
                    member = memberList.get(0);
                } else {
                    logger.debug("list is empty");
                }
            }
            if (member != null) {
                arguments.add(member);
                logger.debug("converted to member: " + member.getEffectiveName());
                continue;
            }

            try {
                TextChannel channel = guild.getTextChannelById(s);
                if (channel != null) {
                    arguments.add(channel);
                    logger.debug("converted to channel: " + channel.getName());
                    continue;
                }
            } catch (NumberFormatException ignored) {
            }

            List<Member> members = new ArrayList<>();
            try {
                Role role = guild.getRoleById(s);
                if (role != null) {
                    members = guild.getMembersWithRoles(role);
                }
            } catch (NumberFormatException ignored) {
            }
            if (members.isEmpty() && s.equals("all")) {
                members = guild.getMembers();
            }
            if (!members.isEmpty()) {
                arguments.add(members);
                logger.debug("converted to member list with size: " + members.size());
                continue;
            }

            try {
                int j = Integer.parseInt(s);
                arguments.add(j);
                logger.debug("converted to int");
                continue;
            } catch (NumberFormatException ignored) {
            }

            logger.debug("no suitable conversion found");
            arguments.add(s);

        }
        return arguments;
    }

    private boolean isRole(String id) {
        Role role;
        try {
            role = bot.getJda().getRoleById(id);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return role == null;
    }

}





















