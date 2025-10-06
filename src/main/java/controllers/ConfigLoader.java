package controllers;
import java.io.IOException;
import java.util.Properties;


public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(ConfigLoader.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFacebookPageId() {
        return properties.getProperty("facebook.pageId");
    }

    public static String getFacebookAccessToken() {
        return properties.getProperty("facebook.accessToken");
    }
}
