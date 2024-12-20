package me.kermx.prismaUtils.Utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.ZoneId;

public class TimeZoneInfo {
    private JSONObject data;

    public TimeZoneInfo(InetSocketAddress address) throws IOException {
        String ip = address.getAddress().getHostAddress();

        try {
            this.data = getJSON(ip);
        } catch (Exception e) {
            this.data = null;
        }
    }

    private JSONObject getJSON(String ip) throws Exception {
        StringBuilder response = new StringBuilder();
        URL url = new URL("http://ip-api.com/json/" + ip);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return (JSONObject) new JSONParser().parse(response.toString());
    }

    public String getData(String key) {
        if (data == null) return "API Down";
        return data.containsKey(key) ? data.get(key).toString() : "invalid identifier";
    }

    public ZoneId getZoneId() {
        if (data == null) {
            return null;
        }

        String tzString = getData("timezone");
        // If API down or invalid identifier return null
        if ("API Down".equals(tzString) || "invalid identifier".equals(tzString)) {
            return null;
        }

        try {
            return ZoneId.of(tzString);
        } catch (Exception e) {
            // If the timezone string can't be parsed return null
            return null;
        }
    }
}
