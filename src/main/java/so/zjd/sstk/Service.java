package so.zjd.sstk;

import java.io.IOException;
import java.net.URISyntaxException;
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

import so.zjd.sstk.util.IOUtils;
import so.zjd.sstk.util.MailSender;
import so.zjd.sstk.util.PathUtils;

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
	private ExecutorService resourceDownloadService;

	public void launch(String[] urls) {
		LOGGER.debug("Service startup. urls：" + Arrays.toString(urls));
		pageService = Executors.newFixedThreadPool(urls.length);
		resourceDownloadService = Executors.newFixedThreadPool(urls.length * 2);
		List<FutureTask<Boolean>> tasks = new ArrayList<>(urls.length);
		for (final String url : urls) {
			FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					try (PageEntry page = new PageEntry(url)) {
						LOGGER.debug(page.toString());
						new PageParser(resourceDownloadService, page).parse();
						generateMobiFile(page);
						sendToKindle(page);
					}
					return true;
				}
			});
			tasks.add(task);
			pageService.submit(task);
		}
		waitServiceCompleted(tasks);
	}

	private void generateMobiFile(PageEntry page) throws IOException, URISyntaxException {
		String kindlegenPath = PathUtils.getRealPath("classpath:bin/kindlegen.exe");
		String cmdStr = String.format(kindlegenPath + " %s -c1 -locale zh", page.getSavePath());
		Process process;
		process = Runtime.getRuntime().exec(cmdStr);
		try {
			// process.waitFor();
			String result = new String(IOUtils.read(process.getInputStream()));
			LOGGER.debug("kindlegen output info:" + result);
		} catch (Exception e) {
			LOGGER.error("kindlegen error.", e);
		}
	}

	private void sendToKindle(PageEntry page) {
		if (!GlobalConfig.DEBUG_SEND_MAIL) {
			return;
		}
		try {
			MailSender mailSender = new MailSender(GlobalConfig.CONFIGS);
			mailSender.sendFrom(page.getTitle(), page.getMobiFilePath());
			LOGGER.debug("sended mobi file to：" + GlobalConfig.CONFIGS.getProperty("mail.to"));
		} catch (Exception e) {
			LOGGER.error("send mail error.", e);
		}

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
		resourceDownloadService.shutdown();
		resourceDownloadService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
		pageService.shutdown();
		pageService.awaitTermination(GlobalConfig.SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
		LOGGER.debug("Service stoped.");
	}

	public static void usage() {
		String usage = "Usage:java -Dfile.encoding=utf-8 -jar SimpleSendToKindle.jar http://xxx1.xxx.xx http:xxx2.xxx.xx ...";
		LOGGER.debug(usage);
	}

	public static void main(String[] args) {
		String url = "";
		if (args.length == 0) {
			usage();
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
