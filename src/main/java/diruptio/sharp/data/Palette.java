package diruptio.sharp.data;

import org.jetbrains.annotations.NotNull;

public class Palette<T> {
    private final T[] palette;

    public Palette(@NotNull T[] palette) {
        this.palette = palette;
    }

    public @NotNull T get(int index) {
        if (index < 0 || index >= palette.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return palette[index];
    }

    public int getId(@NotNull T blockType) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i].equals(blockType)) {
                return i;
            }
        }
        return -1;
    }

    public int getSize() {
        return palette.length;
    }
}
