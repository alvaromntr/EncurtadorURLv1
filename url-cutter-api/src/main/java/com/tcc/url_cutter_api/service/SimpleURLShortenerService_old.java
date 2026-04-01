package com.tcc.url_cutter_api.service;

import java.util.HashMap;
import java.util.Map;

public class SimpleURLShortenerService_old {

    // Caracteres Base62
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Tipo Maps para armazenar url
    private static Map<String, String> shortToLong = new HashMap<>();
    private static Map<String, String> longToShort = new HashMap<>();

    // Contador (simula auto-incremento do db)
    private static long counter = 1;

    private static final String BASE_URL = "http://tiny.url/";

    // Encurta as urls longas
    public static String encode(String longUrl) {

        // Caso já exista, retorna a url encurtada
        if (longToShort.containsKey(longUrl)) {
            return BASE_URL + longToShort.get(longUrl);
        }

        // Gera código base 62
        String shortKey = convertToBase62(counter);
        counter++;

        // Armazena no mapping
        shortToLong.put(shortKey, longUrl);
        longToShort.put(longUrl, shortKey);

        return BASE_URL + shortKey;
    }

    // Decodifica url
    public static String decode(String shortUrl) {

        String shortKey = shortUrl.replace(BASE_URL, "");
        return shortToLong.getOrDefault(shortKey, "URL not found");
    }

    // Metodo que converte para Base62
    private static String convertToBase62(long num) {

        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int remainder = (int)(num % 62);
            sb.append(BASE62.charAt(remainder));
            num = num / 62;
        }

        return sb.reverse().toString();
    }

}
