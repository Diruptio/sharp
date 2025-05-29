package diruptio.sharp.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockUtil {
    public static @Nullable CompoundTag getBlockEntityNbt(@NotNull Block block) {
        ServerLevel level = ((CraftWorld) block.getWorld()).getHandle();
        LevelChunk levelChunk = level.getChunk(block.getChunk().getX(), block.getChunk().getZ());
        BlockPos pos = ((CraftBlock) block).getPosition();
        return levelChunk.getBlockEntityNbtForSaving(pos, level.registryAccess());
    }
}
