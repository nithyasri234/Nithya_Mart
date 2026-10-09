package com.nithyamart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class JsonUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = createGson();

    private JsonUtil() {
    }

    public static Gson getGson() {
        return GSON;
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                src != null
                                        ? new JsonPrimitive(src.format(FORMATTER))
                                        : null
                )
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                            if (json == null || json.isJsonNull() || json.getAsString().isBlank()) {
                                return null;
                            }
                            String text = json.getAsString().trim();
                            try {
                                return LocalDateTime.parse(text, FORMATTER);
                            } catch (Exception e) {
                                try {
                                    return OffsetDateTime.parse(text).toLocalDateTime();
                                } catch (Exception ignored) {
                                    return LocalDateTime.parse(text);
                                }
                            }
                        }
                )
                .create();
    }
}
