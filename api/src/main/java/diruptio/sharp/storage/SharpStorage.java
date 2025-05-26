package diruptio.sharp.storage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jetbrains.annotations.NotNull;

public interface SharpStorage {
    boolean exists(@NotNull String name);
    @NotNull ObjectInputStream openInputStream(@NotNull String name);
    @NotNull ObjectOutputStream openOutputStream(@NotNull String name);
}
