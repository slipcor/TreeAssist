package net.slipcor.treeassist.utils;

import java.util.*;

public final class StringUtils {
	private StringUtils() {
	}

    /**
     * Construct a string out of an array
     *
     * @param array the array to read
     * @param glue the separator to insert
     * @return the constructed string
     */
    public static String joinArray(final Object[] array, final String glue) {
        final StringBuilder result = new StringBuilder();
        for (final Object o : array) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
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


}
