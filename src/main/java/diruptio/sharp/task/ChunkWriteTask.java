package diruptio.sharp.task;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record ChunkWriteTask(@NotNull Chunk chunk, @NotNull BlockPalette blockPalette) implements Callable<ByteBuffer> {
    @Override
    public ByteBuffer call() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        int blockId = 0;
        int currentBlockCount = 0;
        int heaps = 0;
        for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material type = chunk.getBlock(x, y, z).getType();
                    int currentBlockId = blockPalette.getBlockId(new BlockType(type.key()));
                    if (currentBlockId == -1 || currentBlockId == blockId) {
                        currentBlockCount++;
                    } else {
                        if (currentBlockCount > 0) {
                            out.writeInt(currentBlockCount);
                            out.writeInt(blockId);
                            heaps++;
                        }
                        blockId = currentBlockId;
                        currentBlockCount = 1;
                    }
                }
            }
        }
        if (currentBlockCount > 0) {
            out.writeInt(currentBlockCount);
            out.writeInt(blockId);
            heaps++;
        }

        byte[] data = out.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(3 * Integer.BYTES + data.length);
        buffer.putInt(chunk.getX());
        buffer.putInt(chunk.getZ());
        buffer.putInt(heaps);
        buffer.put(data);

        return buffer;
    }
}
