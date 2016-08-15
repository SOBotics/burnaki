package fr.tunaki.stackoverflow.burnaki.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Utils {
	
	private Utils() { }

	public static <T, A, B> Collector<T, ?, List<B>> unorderedBatchesWith(int batchSize, Collector<T, A, B> batchCollector) {
		return unorderedBatches(batchSize, Collectors.mapping(list -> list.stream().collect(batchCollector), Collectors.toList()));
	}

	private static <T, A, R> Collector<T, ?, R> unorderedBatches(int batchSize, Collector<List<T>, A, R> downstream) {
		class Acc {
			List<T> cur = new ArrayList<>();
			A acc = downstream.supplier().get();
		}
		BiConsumer<Acc, T> accumulator = (acc, t) -> {
			acc.cur.add(t);
			if (acc.cur.size() == batchSize) {
				downstream.accumulator().accept(acc.acc, acc.cur);
				acc.cur = new ArrayList<>();
			}
		};
		return Collector.of(Acc::new, accumulator, (acc1, acc2) -> {
			acc1.acc = downstream.combiner().apply(acc1.acc, acc2.acc);
			for (T t : acc2.cur)
				accumulator.accept(acc1, t);
			return acc1;
		}, acc -> {
			if (!acc.cur.isEmpty())
				downstream.accumulator().accept(acc.acc, acc.cur);
			return downstream.finisher().apply(acc.acc);
		}, Collector.Characteristics.UNORDERED);
	}

}
