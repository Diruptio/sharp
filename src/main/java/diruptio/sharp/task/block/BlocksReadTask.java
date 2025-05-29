package diruptio.sharp.task.block;

import diruptio.sharp.data.Palette;
import diruptio.sharp.util.BitBuffer;
import java.util.concurrent.Callable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

public record BlocksReadTask(@NotNull World world,
                             @NotNull Palette<BlockData> blockPalette,
                             @NotNull Palette<CompoundTag> blockEntityPalette,
                             @NotNull BitBuffer buffer) implements Callable<Void> {
    @Override
    public Void call() {
        int chunkX = buffer.readInt();
        int chunkZ = buffer.readInt();
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        ServerLevel level = ((CraftWorld) world).getHandle();
        LevelChunk levelChunk = level.getChunk(chunkX, chunkZ);

        // Read blocks
        int minHeight = world.getMinHeight();
        int blockHeaps = buffer.readInt();
        for (int j = 0, blockIndex = 0; j < blockHeaps; j++) {
            int count = buffer.readInt();
            BlockData blockData = blockPalette.get(buffer.readInt());
            BlockState blockState = ((CraftBlockData) blockData).getState();
            for (int k = 0; k < count; k++, blockIndex++) {
                int y = minHeight + blockIndex / (16 * 16);
                int x = (blockIndex / 16) % 16;
                int z = blockIndex % 16;
                BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                levelChunk.setBlockState(pos, blockState, 530);
            }
        }

        // Read block entities
        int blockEntityCount = buffer.readInt();
        for (int i = 0; i < blockEntityCount; i++) {
            Block block = chunk.getBlock(buffer.readInt(), buffer.readInt(), buffer.readInt());
            CompoundTag nbt = blockEntityPalette.get(buffer.readInt());
            BlockPos pos = ((CraftBlock) block).getPosition();
            BlockState blockState = level.getBlockState(pos);
            BlockEntity blockEntity = BlockEntity.loadStatic(pos, blockState, nbt, level.registryAccess());
            if (blockEntity != null) {
                levelChunk.addAndRegisterBlockEntity(blockEntity);
            }
        }
        return null;
    }
}
