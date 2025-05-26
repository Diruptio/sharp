package diruptio.sharp.task;

import diruptio.sharp.data.BlockHeap;
import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

public record ChunkBlocksReadTask(@NotNull Chunk chunk,
                                  @NotNull BlockPalette blockPalette,
                                  BlockHeap @NotNull [] heaps,
                                  @NotNull CompletableFuture<Void> future) implements Runnable {
    @Override
    public void run() {
        ServerLevel level = ((CraftWorld) chunk.getWorld()).getHandle();
        LevelChunk levelChunk = level.getChunk(chunk.getX(), chunk.getZ());
        int minHeight = chunk.getWorld().getMinHeight();
        int blockIndex = 0;
        for (BlockHeap heap : heaps) {
            BlockType blockType = blockPalette.getBlockType(heap.blockId());
            Material material = Objects.requireNonNull(Material.matchMaterial(blockType.key().asString()));
            BlockState blockState = ((CraftBlockData) material.createBlockData()).getState();
            for (int k = 0; k < heap.count(); k++) {
                int y = minHeight + blockIndex / (16 * 16);
                int x = (blockIndex / 16) % 16;
                int z = blockIndex % 16;
                BlockPos blockPos = new BlockPos(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z);
                levelChunk.setBlockState(blockPos, blockState, 530);
                blockIndex++;
            }
        }
        future.complete(null);
    }
}
