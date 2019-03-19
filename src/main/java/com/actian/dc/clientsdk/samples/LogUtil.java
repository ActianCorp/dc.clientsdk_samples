package com.actian.dc.clientsdk.samples;

import java.util.logging.*;

/**
 * Simple logging utility used by the Client SDK Samples project
 */
public class LogUtil 
{
    static Logger getLogger(Class<?> clazz) {
        // Set the log level to Info and send logs to the console
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setLevel(Level.INFO);
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter()); 
        return logger;
    }
}
