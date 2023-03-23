package com.straight8.rambeau.util;

import java.util.ArrayList;
import java.util.List;

public final class CommandPageUtils {

    private CommandPageUtils() {
    }

    public static final boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static final List<String> getNextInteger(String argument, int maxPage) {
        List<String> options = new ArrayList<>();
        int curNr;
        if (argument.isEmpty()) {
            curNr = 0;
        } else if (!isInteger(argument)) {
            return options;
        } else {
            curNr = Integer.valueOf(argument);
        }
        if (curNr * 10 > maxPage) {
            return options;
        }
        int example = 10 * curNr;
        while (example <= maxPage && example < 10 * (curNr + 1)) {
            if (example != 0) {
                // don't send 0
                options.add(String.valueOf(example));
            }
            example++;
        }
        return options;
    }

}
