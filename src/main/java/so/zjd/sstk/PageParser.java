package so.zjd.sstk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import so.zjd.sstk.util.HttpHelper;
import so.zjd.sstk.util.RegexUtils;

/**
 * 
 * Parse page and download resources.
 * 
 * @author jdzhan,2014-12-6
 * 
 */
public class PageParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(PageParser.class);
	private static final char[] IMG_START_TAG = new char[] { '<', 'i', 'm', 'g' };
	private static final char[] LINK_START_TAG = new char[] { '<', 'l', 'i', 'n', 'k' };
	private static final char[] END_TAG = new char[] { '/', '>' };

	private ExecutorService downloaders;
	private List<FutureTask<Boolean>> futureTasks = new ArrayList<>();

	private PageEntry page;
	private int imgIndex = 0;
	private int linkIndex = 0;

	public PageParser(ExecutorService service, PageEntry page) {
		this.downloaders = service;
		this.page = page;
	}

	public void parse() throws InterruptedException, IOException, URISyntaxException {
		processResources(page);
		waitDownloadCompleted();
		page.save();
	}

	protected void processResources(PageEntry page) {
		StringBuilder processed = new StringBuilder();
		StringBuilder element = new StringBuilder();
		StringBuilder content = page.getContent();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			// if (linkIndex < 5 && c == LINK_START_TAG[linkIndex]) {
			// linkIndex++;
			// } else {
			// linkIndex = 0;
			// }
			if (imgIndex < 4 && c == IMG_START_TAG[imgIndex]) {
				imgIndex++;
			} else {
				imgIndex = 0;
			}
			// 因为保留CSS会导致生成的页面在Kindle上展示错乱，所以就不下载了。
			// if (linkIndex == 5) {// <link
			// processed.delete(processed.length() - 4, processed.length());
			// element.append("<link");
			// linkIndex = 0;
			// while (i < content.length() - 1) {
			// c = content.charAt(++i);
			// element.append(c);
			// if (linkIndex < 2 && c == END_TAG[linkIndex]) {
			// linkIndex++;
			// } else {
			// linkIndex = 0;
			// }
			// if (linkIndex == 2) {
			// linkIndex = 0;
			// break;
			// }
			// }
			// processed.append(downloadResource(element.toString(), 1));
			// element.delete(0, element.length());
			// } else
			if (imgIndex == 4) {// <img
				processed.delete(processed.length() - 3, processed.length());
				element.append("<img");
				imgIndex = 0;
				while (i < content.length() - 1) {
					c = content.charAt(++i);
					element.append(c);
					if (imgIndex < 2 && c == END_TAG[imgIndex]) {
						imgIndex++;
					} else {
						imgIndex = 0;
					}
					if (imgIndex == 2) {
						imgIndex = 0;
						break;
					}
				}
				processed.append(downloadResource(element.toString(), 0));
				element.delete(0, element.length());
			} else {
				processed.append(c);
			}
		}
		page.setContent(processed);
	}

	// 0:<img
	// 1:<link
	private String downloadResource(String element, int type) {
		String pattern = "";
		if (type == 0) {
			pattern = "(?<=src=\").*?(?=\")";
		} else if (type == 1) {
			pattern = "(?<=href=\").*?(?=\")";
		} else {
			return element;
		}

		List<String> matchs = RegexUtils.findAll(pattern, element, false);
		if (matchs.isEmpty()) {
			return element;
		}
		String url = processRelativeUrl(matchs.get(0));
		final String fileName = getFileName(url);
		final String result = RegexUtils.replaceAll(pattern, element, GlobalConfig.RESOURCE_DIR_NAME + "/" + fileName,
				false);
		final ResourceEntry res = new ResourceEntry(fileName, url, page.getResourceDir() + fileName);
		FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try (OutputStream os = new FileOutputStream(res.getSavePath())) {
					HttpHelper.download(res.getDownloadUrl(), GlobalConfig.DOWNLOAD_TIMEOUT, os);
					LOGGER.debug("downloaded resource:" + res.toString());
				} catch (Exception e) {
					LOGGER.error("download resource error:" + res.getDownloadUrl(), e);
				}
				return true;
			}
		});
		downloaders.submit(task);
		futureTasks.add(task);
		return result;
	}

	// ./images/mem/figure9.png
	// images/mem/figure9.png
	// /images/mem/figure9.png
	private String processRelativeUrl(String url) {
		if (url.startsWith("http://")) {
			return url;
		}
		String pageUrl = page.getUrl();

		if (url.startsWith("/")) {
			int index = pageUrl.indexOf('/', 7);
			pageUrl = pageUrl.substring(0, index + 1);
		} else {
			int index = pageUrl.lastIndexOf("/");
			pageUrl = pageUrl.substring(0, index + 1);
		}
		imgIndex = url.indexOf("/");
		if (imgIndex != -1) {
			url = url.substring(imgIndex + 1);
		}
		url = pageUrl + url;

		return url;
	}

	private void waitDownloadCompleted() {
		for (FutureTask<Boolean> task : futureTasks) {
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("download image error.", e);
			}
		}
	}

	private String getFileName(String url) {
		int index = url.lastIndexOf("/");
		String fileName = "";
		if (index != -1) {
			fileName = url.substring(index + 1);
		}
		index = fileName.indexOf("?");
		if (index != -1) {
			fileName = fileName.substring(0, index);
		}
		return fileName;
	}
}
