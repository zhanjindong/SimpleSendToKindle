package so.zjd.sstk;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service {
	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
	private static ExecutorService service = Executors.newFixedThreadPool(2);

	static {
		GlobalConfig.init();
	}

	public void launch(String[] urls) {
		for (final String url : urls) {
			service.submit(new Runnable() {
				@Override
				public void run() {
					try (PageEntry page = new PageEntry(url)) {
						LOGGER.debug(page.toString());
						new MobiCreator(page).create();
					}
				}
			});
		}
		service.shutdown();
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
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
		service.launch(args);
	}
}
