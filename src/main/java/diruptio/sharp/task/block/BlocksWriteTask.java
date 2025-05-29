package diruptio.sharp.task.block;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import diruptio.sharp.util.BlockUtil;
import java.util.concurrent.Callable;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public record BlocksWriteTask(@NotNull Chunk chunk,
                              @NotNull Palette<BlockData> blockPalette,
                              @NotNull Palette<CompoundTag> blockEntityPalette) implements Callable<BitBuffer> {
    @Override
    public BitBuffer call() {
        BitBuffer blockBuffer = new BitBuffer(blockPalette.getSize() * 4);
        int blockId = 0;
        int currentBlockCount = 0;
        int blockHeaps = 0;

        BitBuffer blockEntityBuffer = new BitBuffer(blockEntityPalette.getSize());
        int blockEntityCount = 0;

        int minHeight = chunk.getWorld().getMinHeight();
        int maxHeight = chunk.getWorld().getMaxHeight();
        for (int y = minHeight; y < maxHeight; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);

                    // Write block data
                    int currentBlockId = blockPalette.getId(block.getBlockData());
                    if (currentBlockId == -1 || currentBlockId == blockId) {
                        currentBlockCount++;
                    } else {
                        if (currentBlockCount > 0) {
                            blockBuffer.writeInt(currentBlockCount);
                            blockBuffer.writeInt(blockId);
                            blockHeaps++;
                        }
                        blockId = currentBlockId;
                        currentBlockCount = 1;
                    }

                    // Write block entity data
                    CompoundTag blockEntityNbt = BlockUtil.getBlockEntityNbt(block);
                    if (blockEntityNbt != null) {
                        int blockEntityId = blockEntityPalette.getId(blockEntityNbt);
                        if (blockEntityId != -1) {
                            blockEntityBuffer.writeInt(x);
                            blockEntityBuffer.writeInt(y);
                            blockEntityBuffer.writeInt(z);
                            blockEntityBuffer.writeInt(blockEntityId);
                            blockEntityCount++;
                        }
                    }
                }
            }
        }

        // Write the last block data
        if (currentBlockCount > 0) {
            blockBuffer.writeInt(currentBlockCount);
            blockBuffer.writeInt(blockId);
            blockHeaps++;
        }

        blockBuffer.flip();
        blockEntityBuffer.flip();
        BitBuffer buffer = new BitBuffer(blockBuffer.getSize());
        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());
        buffer.writeInt(blockHeaps);
        buffer.writeBits(blockBuffer);
        buffer.writeInt(blockEntityCount);
        buffer.writeBits(blockEntityBuffer);

        return buffer;
    }
}
