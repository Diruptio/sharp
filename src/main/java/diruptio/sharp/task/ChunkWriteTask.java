package diruptio.sharp.task;

import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import diruptio.sharp.util.BitBuffer;
import java.util.concurrent.Callable;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record ChunkWriteTask(@NotNull Chunk chunk, @NotNull BlockPalette blockPalette) implements Callable<BitBuffer> {
    @Override
    public BitBuffer call() {
        BitBuffer blockBuffer = new BitBuffer(blockPalette.getSize() * 4);
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
                            blockBuffer.writeInt(currentBlockCount);
                            blockBuffer.writeInt(blockId);
                            heaps++;
                        }
                        blockId = currentBlockId;
                        currentBlockCount = 1;
                    }
                }
            }
        }
        if (currentBlockCount > 0) {
            blockBuffer.writeInt(currentBlockCount);
            blockBuffer.writeInt(blockId);
            heaps++;
        }
        blockBuffer.flip();

        BitBuffer buffer = new BitBuffer(blockBuffer.getSize());
        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());
        buffer.writeInt(heaps);
        buffer.writeBits(blockBuffer);

        return buffer;
    }
}
