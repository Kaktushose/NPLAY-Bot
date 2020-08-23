package de.kaktushose.nrtv.discord.frameworks.command;

import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

public class Arguments {

    private Map<Integer, List> values;
    private int lastIndex;

    public Arguments() {
        values = new HashMap<>();
        lastIndex = 0;
    }

    public Arguments(String[] values) {
        Arrays.asList(values).forEach(this::add);
    }

    public int getSize() {
        return lastIndex + 1;
    }

    Arguments add(String value) {
        values.put(lastIndex, Collections.singletonList(value));
        lastIndex++;
        return this;
    }

    Arguments add(int value) {
        values.put(lastIndex, Collections.singletonList(value));
        lastIndex++;
        return this;
    }

    Arguments add(Member member) {
        values.put(lastIndex, Collections.singletonList(member));
        lastIndex++;
        return this;
    }

    Arguments add(TextChannel channel) {
        values.put(lastIndex, Collections.singletonList(channel));
        lastIndex++;
        return this;
    }

    Arguments add(List<Member> members) {
        values.put(lastIndex, members);
        lastIndex++;
        return this;
    }

    public String getAsString(int index) {
        return String.valueOf(values.get(index).get(0));
    }

    public int getAsInteger(int index) {
        return Integer.parseInt(String.valueOf(values.get(index).get(0)));
    }

    public Member getAsMember(int index) {
        return (Member) values.get(index).get(0);
    }

    public TextChannel getAsChannel(int index) {
        return (TextChannel) values.get(index).get(0);
    }

    public List<Member> getAsMemberList(int index) {
        return (List<Member>) values.get(index);
    }

    boolean isInteger(int index) {
        try {
            Integer.parseInt(String.valueOf(values.get(index).get(0)));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    boolean isChannel(int index) {
        try {
            TextChannel channel = (TextChannel) values.get(index).get(0);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    boolean isMemberList(int index) {
        try {
            List<Member> members = (List<Member>) values.get(index).get(0);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    boolean isMember(int index) {
        try {
            Member member = (Member) values.get(index).get(0);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

}
