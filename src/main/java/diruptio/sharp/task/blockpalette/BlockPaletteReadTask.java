package diruptio.sharp.task.blockpalette;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record BlockPaletteReadTask(@NotNull BitBuffer buffer) implements Callable<Palette<BlockData>> {
    public Palette<BlockData> call() {
        long start = System.currentTimeMillis();

        int size = buffer.readInt();
        BlockData[] palette = new BlockData[size];
        for (int i = 0; i < size; i++) {
            palette[i] = Bukkit.createBlockData(buffer.readString());
        }

        debugPerformance("Read block palette with size " + size, start);
        return new Palette<>(palette);
    }
}
