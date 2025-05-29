package diruptio.sharp.task.block;

import diruptio.sharp.util.BlockUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

public record BlockEntityCountTask(@NotNull Chunk chunk) implements Callable<Map<CompoundTag, Integer>> {
    @Override
    public Map<CompoundTag, Integer> call() {
        Map<CompoundTag, Integer> blocks = new HashMap<>();
        for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    CompoundTag nbt = BlockUtil.getBlockEntityNbt(chunk.getBlock(x, y, z));
                    if (nbt != null) {
                        blocks.put(nbt, blocks.getOrDefault(nbt, 0) + 1);
                    }
                }
            }
        }
        return blocks;
    }
}
