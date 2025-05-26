package diruptio.sharp.data;

import net.kyori.adventure.key.Key;

public record BlockType(Key key) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockType(Key otherKey) && key.equals(otherKey);
    }
}
