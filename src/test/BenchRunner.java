package lists;

import java.util.AbstractSet;

@FunctionalInterface
interface BenchRunner {
    long bench(AbstractSet<Integer> list, int rounds) throws Exception;
}
