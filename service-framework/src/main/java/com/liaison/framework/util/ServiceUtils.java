package com.liaison.framework.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URL;

/**
 * Service Utils
 * <p/>
 * <P>Grab bag of utilities used by service framework and implementation
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class ServiceUtils {

    public static String readFileFromClassPath(String path) {

        if (path.startsWith("classpath://")) {
            path = path.replace("classpath://", "/");
        }

        URL url = Resources.getResource(path);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving " + path + " from classpath.", e);
        }
    }

    public static String prettifyJSON(String rawJSON) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(rawJSON);
        String pretty = gson.toJson(je);
        return pretty;
    }

    public static void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }


    public static String formatArrayAsString(Object[] array, String separator) {

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            s.append(array[i]);
            if (i < array.length - 1) {
                s.append(separator);
            }
        }

        return s.toString();
    }


}
