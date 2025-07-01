package utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.StringUtils;

public class EnvUtils {
    private static Dotenv dotenv;

    public static Dotenv Env() {
        if (dotenv == null) {
            String profile = "local";
            String currentProfile = System.getenv("testEnv");
            if (StringUtils.isNoneBlank(currentProfile)) {
                profile = currentProfile;
            }
            dotenv = Dotenv.configure()
                    .directory("/env")
                    .filename(String.format(".env.%s", profile))
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
        }
        return dotenv;
    }
}
