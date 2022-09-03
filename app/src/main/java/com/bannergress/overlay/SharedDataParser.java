package com.bannergress.overlay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SharedDataParser {
    private static final Pattern pattern = Pattern.compile("https://bannergress.com/banner/([^\\s]+)|https://intel.ingress.com/mission/([0-9a-f]{32}\\.1c)|https%3a%2f%2fintel.ingress.com%2fmission%2f([0-9a-f]{32}\\.1c)");

    static ParsedData parse(String data) {
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find()) {
            return new ParsedData(ParsedDataType.invalid, null);
        } else if (matcher.group(1) != null) {
            return new ParsedData(ParsedDataType.banner, matcher.group(1));
        } else if (matcher.group(2) != null) {
            return new ParsedData(ParsedDataType.mission, matcher.group(2));
        } else {
            return new ParsedData(ParsedDataType.mission, matcher.group(3));
        }
    }

    enum ParsedDataType {
        mission,
        banner,
        invalid
    }

    static class ParsedData {
        public final ParsedDataType type;

        public final String id;

        private ParsedData(ParsedDataType type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
