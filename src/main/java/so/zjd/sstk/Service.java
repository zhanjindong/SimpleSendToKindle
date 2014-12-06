package so.zjd.sstk;

import java.util.ArrayList;
import java.util.Arrays;
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

import so.zjd.sstk.util.MailSender;

/**
 * 
 * The service main class.
 * 
 * @author jdzhan,2014-12-6
 * 
 */
public class Service implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

	private ExecutorService pageService;
	private ExecutorService imageDownloadService;

	public void launch(String[] urls) throws InterruptedException {
		LOGGER.debug("Service startup. urls：" + Arrays.toString(urls));
		pageService = Executors.newFixedThreadPool(urls.length);
		imageDownloadService = Executors.newFixedThreadPool(urls.length * 2);
		List<FutureTask<Boolean>> tasks = new ArrayList<>(urls.length);
		for (final String url : urls) {
			FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					try (PageEntry page = new PageEntry(url)) {
						LOGGER.debug(page.toString());
						try {
							new MobiGenerator(imageDownloadService, page).handle();
							sendToKindle(page);
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

	private void sendToKindle(PageEntry page) {
		if (!GlobalConfig.DEBUG_SEND_MAIL) {
			return;
		}
		MailSender mailSender = new MailSender(GlobalConfig.CONFIGS);
		mailSender.sendFrom(page.getTitle(), page.getMobiFilePath());
		LOGGER.debug("sended mobi file to：" + GlobalConfig.CONFIGS.getProperty("mail.to"));
	}

	private void waitServiceCompleted(List<FutureTask<Boolean>> tasks) {
		for (FutureTask<Boolean> task : tasks) {
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Service error.", e);
			}
		}
	}

	@Override
	public void close() throws Exception {
		imageDownloadService.shutdown();
		imageDownloadService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
		pageService.shutdown();
		pageService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
		LOGGER.debug("Service stoped.");
	}

	public static void report(String msg) {

	}

	public static void main(String[] args) {
		String url = "";
		if (args.length == 0) {
			report("Missing parameter!");
			return;
		}
		url = args[0];
		if (StringUtils.isEmpty(url)) {
			return;
		}
		try (Service service = new Service()) {
			service.launch(args);
		} catch (Throwable e) {
			LOGGER.error("Service error.", e);
		}
	}

}
