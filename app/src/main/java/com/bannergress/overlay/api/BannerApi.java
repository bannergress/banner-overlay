package com.bannergress.overlay.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class BannerApi {
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    private static final ObjectMapper objectMapper;

    private static final String baseUrl = "https://api.bannergress.com";

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static List<Banner> findBanners(String missionId) throws IOException {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();
        urlBuilder.addPathSegment("bnrs");
        urlBuilder.addQueryParameter("missionId", missionId);
        Request request = new Request.Builder().url(urlBuilder.build()).build();
        ResponseBody responseBody = client.newCall(request).execute().body();
        assert responseBody != null;
        return objectMapper.readValue(responseBody.string(), new TypeReference<List<Banner>>() {
        });
    }

    public static Banner getBanner(String bannerId) throws IOException {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();
        urlBuilder.addPathSegment("bnrs");
        urlBuilder.addEncodedPathSegment(bannerId);
        Request request = new Request.Builder().url(urlBuilder.build()).build();
        ResponseBody responseBody = client.newCall(request).execute().body();
        assert responseBody != null;
        return objectMapper.readValue(responseBody.string(), Banner.class);
    }
}
