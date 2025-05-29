package diruptio.sharp.task.block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.bukkit.Chunk;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public record BlockCountTask(@NotNull Chunk chunk) implements Callable<Map<BlockData, Integer>> {
    @Override
    public Map<BlockData, Integer> call() {
        Map<BlockData, Integer> blocks = new HashMap<>();
        for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockData data = chunk.getBlock(x, y, z).getBlockData();
                    blocks.put(data, blocks.getOrDefault(data, 0) + 1);
                }
            }
        }
        return blocks;
    }
}
