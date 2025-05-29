package diruptio.sharp.task.blockpalette;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import java.util.concurrent.Callable;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record BlockPaletteWriteTask(@NotNull Palette<BlockData> palette) implements Callable<BitBuffer> {
    @Override
    public BitBuffer call() {
        long start = System.currentTimeMillis();

        int size = palette.getSize();
        BitBuffer buffer = new BitBuffer();
        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            buffer.writeString(palette.get(i).getAsString());
        }

        debugPerformance("Wrote block palette with size " + size, start);
        return buffer;
    }
}
