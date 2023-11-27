package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;

@Interaction
public class HelloWorldCommand {

    @SlashCommand("hello")
    public void onCommand(CommandEvent event) {
        event.reply("Hello World!");
    }
}
