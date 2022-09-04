package com.bannergress.overlay;

import java.util.Optional;
import java.util.regex.Matcher;

import okhttp3.HttpUrl;

class SharedDataParser {
    static ParsedData parse(String data) {
        Matcher matcher = android.util.Patterns.WEB_URL.matcher(data);
        while (matcher.find()) {
            HttpUrl url = HttpUrl.parse(matcher.group());
            if (url == null) {
                continue;
            }
            Optional<ParsedData> optionalParsedData = parseBannergressBanner(url);
            if (optionalParsedData.isPresent()) {
                return optionalParsedData.get();
            }
            optionalParsedData = parseIntelMission(url);
            if (optionalParsedData.isPresent()) {
                return optionalParsedData.get();
            }
        }
        return new ParsedData(ParsedDataType.invalid, null);
    }

    private static Optional<ParsedData> parseBannergressBanner(HttpUrl url) {
        if (url.isHttps() && url.host().equals("bannergress.com") && url.pathSegments().size() == 2 && url.pathSegments().get(0).equals("banner")) {
            return Optional.of(new ParsedData(ParsedDataType.banner, url.pathSegments().get(1)));
        }
        return Optional.empty();
    }

    private static Optional<ParsedData> parseIntelMission(HttpUrl url) {
        if (url.host().equals("intel.ingress.com") && url.pathSegments().size() == 2 && url.pathSegments().get(0).equals("mission")) {
            return Optional.of(new ParsedData(ParsedDataType.mission, url.pathSegments().get(1)));
        } else if (url.isHttps() && url.host().equals("link.ingress.com")) {
            String linkParameter = url.queryParameter("link");
            if (linkParameter != null) {
                HttpUrl linkUrl = HttpUrl.parse(linkParameter);
                if (linkUrl == null) {
                    return Optional.empty();
                }
                return parseIntelMission(linkUrl);
            }
        }
        return Optional.empty();
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
