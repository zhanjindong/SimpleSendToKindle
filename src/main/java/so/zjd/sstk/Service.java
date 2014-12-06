package so.zjd.sstk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

	private ExecutorService pageService;
	private ExecutorService imgDownloaderService;

	public void launch(String[] urls) throws InterruptedException {
		pageService = Executors.newFixedThreadPool(urls.length);
		imgDownloaderService = Executors.newFixedThreadPool(urls.length * 2);
		List<FutureTask<Boolean>> tasks = new ArrayList<>(urls.length);
		for (final String url : urls) {
			FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					try (PageEntry page = new PageEntry(url)) {
						LOGGER.debug(page.toString());
						try {
							new MobiCreator(imgDownloaderService, page).create();
						} catch (Throwable e) {
							LOGGER.error("mobi create error. title:" + page.getTitle(), e);
						}
					}
					return true;
				}
			});
			tasks.add(task);
			pageService.submit(task);
		}
		waitServiceCompleted(tasks);
	}

	private void waitServiceCompleted(List<FutureTask<Boolean>> tasks) {
		for (FutureTask<Boolean> task : tasks) {
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("service error.", e);
			}
		}
	}

	@Override
	public void close() throws Exception {
		imgDownloaderService.shutdown();
		imgDownloaderService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
		pageService.shutdown();
		pageService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	public static void main(String[] args) {
		String url = "";
		if (args.length == 0) {
			return;
		}
		url = args[0];
		if (StringUtils.isEmpty(url)) {
			return;
		}
		LOGGER.debug("url:" + url);

		try (Service service = new Service()) {
			service.launch(args);
		} catch (Throwable e) {
			LOGGER.error("service error.", e);
		}
	}

}
