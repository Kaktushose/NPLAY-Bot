package de.kaktushose.nrtv.discord.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {

    public static Logger getLogger() {
        return LoggerFactory.getLogger(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
    }

}
