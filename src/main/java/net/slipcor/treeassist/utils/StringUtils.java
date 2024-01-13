package net.slipcor.treeassist.utils;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {
	private StringUtils() {
	}

    /**
     * Check whether a given string is contained loosely in a list of strings
     *
     * @param list the list to look through
     * @param needle the string to find
     * @param partial allow partial matches
     * @return whether we found a match
     */
    public static boolean matchContains(List<String> list, String needle, boolean partial) {
        if (list.contains(needle)) {
            // The entry is contained perfectly
            return true;
        }
        for (String entry : list) {
            if (partial && entry.toLowerCase().contains(needle.toLowerCase())) {
                return true;
            }
            if (entry.contains("*")) {
                String compare = entry.replace("*", "");
                if (needle.toLowerCase().contains(compare.toLowerCase())) {
                    return true;
                }
            } else if (entry.equalsIgnoreCase(needle)) {
                return true;
            }
        }
        return false;
    }


    public static String[] compress(String[] args) {
        String fullCommand = org.apache.commons.lang.StringUtils.join(args, ' ');
        if (!fullCommand.contains("\"")) {
            return args;
        }
        List<String> arguments = new ArrayList<>();
        String[] quoteString = fullCommand.split("\"");
        int pos = 0;
        for (String entry : quoteString) {
            pos++;
            if (pos%2 == 1) {
                // not inside quotation marks, let us split it by spaces!
                String[] spaced = entry.split(" ");
                for (String space : spaced) {
                    if (space.isEmpty()) {
                        continue;
                    }
                    arguments.add(space);
                }
                continue;
            }
            // inside quotation marks, we add what we find
            if (entry.isEmpty()) {
                continue;
            }
            arguments.add(entry);
        }
        return arguments.toArray(new String[0]);
    }

    /**
     * Check whether a version is greater or equal to our server version
     * @param serverVersion the running server minecraft version as integer array
     * @param testVersion the required server minecraft version as integer array
     * @return whether a particular check version is greater or equal to a required one
     */
    public static boolean isSupportedVersion(int[] serverVersion, int[] testVersion) {
        for (int i=0; i<3; i++) {
            if (serverVersion[i] < testVersion[i]) {
                return false;
            }
            if (serverVersion[i] > testVersion[i]) {
                return true;
            }
        }
        return true;
    }

    /**
     * Create an integer array of the Bukkit version string
     * @param version the bukkit version string, i.e. "1.20.4-R0.1....."
     * @return an integer array, i.e. {1, 20, 4}
     */
    public static int[] splitToVersionArray(String version) {
        String[] chunks = new String[]{"1", "9"};
        try {
            chunks = version.split("-")[0].split("\\.");
        } catch (Exception e) {
        }
        int major = 1;
        int minor = 9;
        int patch = 0;
        try {
            major = Integer.parseInt(chunks[0]);
        } catch (Exception e) {
        }
        try {
            minor = Integer.parseInt(chunks[1]);
        } catch (Exception e) {
        }
        try {
            patch = Integer.parseInt(chunks[2]);
        } catch (Exception e) {
        }
        return new int[] {major,minor,patch};
    }
}
