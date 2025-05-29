package diruptio.sharp.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;

public interface TaskExecutor {
    <T> @NotNull Future<T> execute(@NotNull Callable<T> task);
}
