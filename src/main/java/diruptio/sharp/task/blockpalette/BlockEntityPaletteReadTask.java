package diruptio.sharp.task.blockpalette;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.*;

public record BlockEntityPaletteReadTask(@NotNull BitBuffer buffer) implements Callable<Palette<CompoundTag>> {
    public Palette<CompoundTag> call() throws IOException {
        long start = System.currentTimeMillis();

        int size = buffer.readInt();
        CompoundTag[] palette = new CompoundTag[size];
        for (int i = 0; i < size; i++) {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer.readBytes(buffer.readInt()));
            palette[i] = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        }

        debugPerformance("Read block-entity palette with size " + size, start);
        return new Palette<>(palette);
    }
}
