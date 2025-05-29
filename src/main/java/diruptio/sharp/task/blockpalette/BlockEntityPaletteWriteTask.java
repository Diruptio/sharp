package diruptio.sharp.task.blockpalette;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record BlockEntityPaletteWriteTask(@NotNull Palette<CompoundTag> palette) implements Callable<BitBuffer> {
    @Override
    public BitBuffer call() throws IOException {
        long start = System.currentTimeMillis();

        int size = palette.getSize();
        BitBuffer buffer = new BitBuffer();
        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.writeCompressed(palette.get(i), out);
            byte[] data = out.toByteArray();
            buffer.writeInt(data.length);
            buffer.writeBytes(data);
        }

        debugPerformance("Wrote block-entity palette with size " + size, start);
        return buffer;
    }
}
