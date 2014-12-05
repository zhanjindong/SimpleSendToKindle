package so.zjd.sstk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service {
	private static ExecutorService service = Executors.newFixedThreadPool(GlobalConfig.SERVICE_THREADS);
	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

	public void launch(String[] urls) throws InterruptedException {
		for (final String url : urls) {
			try (PageEntry page = new PageEntry(url)) {
				LOGGER.debug(page.toString());
				try {
					new MobiCreator(page).create();
				} catch (Throwable e) {
					LOGGER.error("service error.", e);
				}
			}
		}
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
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

		Service service = new Service();
		try {
			service.launch(args);
		} catch (Throwable e) {
			LOGGER.error("service error.", e);
		}
	}
}
