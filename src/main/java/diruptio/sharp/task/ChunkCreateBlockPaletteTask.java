package diruptio.sharp.task;

import diruptio.sharp.data.BlockType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record ChunkCreateBlockPaletteTask(@NotNull Chunk chunk) implements Callable<Map<BlockType, Integer>> {
    @Override
    public Map<BlockType, Integer> call() {
        Map<BlockType, Integer> blocks = new HashMap<>();
        for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material material = chunk.getBlock(x, y, z).getType();
                    BlockType blockType = new BlockType(material.key());
                    blocks.put(blockType, blocks.getOrDefault(blockType, 0) + 1);
                }
            }
        }
        return blocks;
    }
}
