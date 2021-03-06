package nxt.util;

import nxt.Nxt;
import nxt.addons.JO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLookup {

    public static JO loadJsonResource(String resourceName) {
        try (Reader reader = loadResourceText(resourceName)) {
            if (reader == null) {
                return null;
            }
            return JO.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JO loadJsonResource(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            return JO.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Reader loadResourceText(String resourceName) {
        InputStream is = loadResourceBytes(resourceName);
        if (is == null) {
            return null;
        }
        return new InputStreamReader(is);
    }

    public static InputStream loadResourceBytes(String resourceName) {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (resource != null) {
            Logger.logInfoMessage("Loading resource from classpath " + resourceName);
            return resource;
        } else {
            Path path = Paths.get(Nxt.getUserHomeDir(), resourceName);
            if (!Files.isReadable(path)) {
                path = Paths.get(resourceName);
                if (!Files.isReadable(path)) {
                    Logger.logErrorMessage("file not found " + path.toAbsolutePath());
                    return null;
                }
            }
            Logger.logInfoMessage("Loading file from path " + path.toAbsolutePath());
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                Logger.logErrorMessage("Cannot read json file from path " + path.toAbsolutePath(), e);
                return null;
            }
        }
    }

    public static byte[] getResourceBytes(String resourceName) {
        try (InputStream is = loadResourceBytes(resourceName)) {
            if (is == null) {
                return null;
            }
            return readInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static byte[] readInputStream(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return buffer.toByteArray();
    }
}
