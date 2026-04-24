package config;

public final class AppConfig {
    private AppConfig() {}

    public static String getApiKey() {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Missing API key. Set GOOGLE_API_KEY or GEMINI_API_KEY."
            );
        }

        return apiKey;
    }
}