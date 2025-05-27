package diruptio.sharp.task;

import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import diruptio.sharp.util.BitBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

public record ChunkBlocksReadTask(@NotNull World world,
                                  @NotNull BlockPalette blockPalette,
                                  @NotNull BitBuffer buffer,
                                  @NotNull CompletableFuture<Void> future) implements Runnable {
    @Override
    public void run() {
        int minHeight = world.getMinHeight();
        int chunkX = buffer.readInt();
        int chunkZ = buffer.readInt();
        ServerLevel level = ((CraftWorld) world).getHandle();
        LevelChunk levelChunk = level.getChunk(chunkX, chunkZ);
        int heaps = buffer.readInt();
        for (int j = 0, blockIndex = 0; j < heaps; j++) {
            int count = buffer.readInt();
            int blockId = buffer.readInt();
            BlockType blockType = blockPalette.getBlockType(blockId);
            Material material = Objects.requireNonNull(Material.matchMaterial(blockType.key().asString()));
            BlockState blockState = ((CraftBlockData) material.createBlockData()).getState();
            for (int k = 0; k < count; k++) {
                int y = minHeight + blockIndex / (16 * 16);
                int x = (blockIndex / 16) % 16;
                int z = blockIndex % 16;
                BlockPos blockPos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                levelChunk.setBlockState(blockPos, blockState, 530);
                blockIndex++;
            }
        }
        future.complete(null);
    }
}
