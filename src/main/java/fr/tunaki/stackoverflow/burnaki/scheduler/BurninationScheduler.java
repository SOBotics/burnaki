package fr.tunaki.stackoverflow.burnaki.scheduler;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.service.BurninationService;

@Component
public class BurninationScheduler implements Closeable {
	
	private BurninationService burninationService;
	private ScheduledExecutorService executorService;
	private BurninationSchedulerProperties properties;
	
	private Map<String, List<ScheduledFuture<?>>> tasks = new ConcurrentHashMap<>();
	
	@Autowired
	public BurninationScheduler(ScheduledExecutorService executorService, BurninationService burninationService, BurninationSchedulerProperties properties) {
		this.executorService = executorService;
		this.burninationService = burninationService;
		this.properties = properties;
	}
	
	public void start(String tag, int roomId, String metaLink) {
		burninationService.start(tag, roomId, metaLink);
		int refreshQuestionsEvery = properties.getRefreshQuestionsEvery();
		int refreshProgressEvery = properties.getRefreshProgressEvery();
		tasks.computeIfAbsent(tag, t -> Arrays.asList(
			executorService.scheduleAtFixedRate(() -> burninationService.update(t, refreshQuestionsEvery), refreshQuestionsEvery, refreshQuestionsEvery, TimeUnit.MINUTES),
			executorService.scheduleAtFixedRate(() -> burninationService.updateProgress(t), refreshProgressEvery, refreshProgressEvery, TimeUnit.MINUTES)
		));
	}
	
	public void stop(String tag) {
		burninationService.stop(tag);
		List<ScheduledFuture<?>> tagTasks = tasks.remove(tag);
		if (tagTasks != null) {
			tagTasks.forEach(t -> t.cancel(false));
		}
	}
	
	@Override
	public void close() throws IOException {
		executorService.shutdownNow();
	}

}
