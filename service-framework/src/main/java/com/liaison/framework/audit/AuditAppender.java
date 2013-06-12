package com.liaison.framework.audit;

import java.util.*;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class AuditAppender extends AppenderSkeleton {

    ArrayList<LoggingEvent> eventsList = new ArrayList();

    // TODO write to file/syslog
    @Override
    protected void append(LoggingEvent event) {

        System.out.println("** Audit Event **");

        final AuditStatement statement;
        try {
            statement = (AuditStatement)event.getMessage();
        } catch (ClassCastException e) {
            System.out.println("Logging event expected to be of type " + AuditStatement.class.toString());
            System.out.println(event.getMessage().toString());
            return;
        }

        System.out.println(statement.toString());

    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

}