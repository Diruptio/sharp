package diruptio.sharp.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class FileStorage implements SharpStorage {
    public FileStorage() {
        try {
            Files.createDirectories(Path.of("sharp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(@NotNull String name) {
        return Files.exists(Path.of("sharp", name + ".sharp"));
    }

    @Override
    public @NotNull ObjectInputStream openInputStream(@NotNull String name) {
        if (!exists(name)) {
            throw new IllegalArgumentException("World " + name + " does not exist");
        }
        try {
            return new ObjectInputStream(Files.newInputStream(Path.of("sharp", name + ".sharp")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ObjectOutputStream openOutputStream(@NotNull String name) {
        try {
            Files.createDirectories(Path.of("sharp"));
            return new ObjectOutputStream(Files.newOutputStream(Path.of("sharp", name + ".sharp")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
