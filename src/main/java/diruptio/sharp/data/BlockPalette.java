package diruptio.sharp.data;

import org.jetbrains.annotations.NotNull;

public class BlockPalette {
    private final BlockType[] palette;

    public BlockPalette(@NotNull BlockType[] palette) {
        this.palette = palette;
    }

    public int getSize() {
        return palette.length;
    }

    public @NotNull BlockType getBlockType(int index) {
        if (index < 0 || index >= palette.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return palette[index];
    }

    public int getBlockId(@NotNull BlockType blockType) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i].equals(blockType)) {
                return i;
            }
        }
        return -1;
    }
}
