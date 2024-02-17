package com.lx862.jbrph.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    public static final String PREFIX = "[RPHelper] ";
    public static final Logger LOGGER = LogManager.getLogger("RPHelperClient");

    public static void info(String s, Object... o) {
        LOGGER.info(PREFIX + s, o);
    }

    public static void warn(String s, Object... o) {
        LOGGER.warn(PREFIX + s, o);
    }

    public static void error(String s, Object... o) {
        LOGGER.error(PREFIX + s, o);
    }

    public static void fatal(String s, Object... o) {
        LOGGER.fatal(PREFIX + s, o);
    }
}
