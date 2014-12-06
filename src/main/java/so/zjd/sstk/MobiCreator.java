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
import so.zjd.sstk.util.IOUtils;
import so.zjd.sstk.util.MailSender;
import so.zjd.sstk.util.PathUtils;
import so.zjd.sstk.util.RegexUtils;

public class MobiCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MobiCreator.class);
	private static final char[] IMG_START_TAG = new char[] { '<', 'i', 'm', 'g' };
	private static final char[] IMG_END_TAG = new char[] { '/', '>' };

	private ExecutorService downloaders;
	private List<FutureTask<Boolean>> futureTasks = new ArrayList<>();

	private PageEntry page;
	private int index = 0;

	public MobiCreator(ExecutorService service, PageEntry page) {
		this.downloaders = service;
		this.page = page;
	}

	public void create() throws InterruptedException, IOException, URISyntaxException {
		processImages(page);
		waitDownloadCompleted();
		generateMobiFile(page);
	}

	private void generateMobiFile(PageEntry page) throws IOException, URISyntaxException {
		page.save();
		String kindlegenPath = PathUtils.getRealPath("classpath:kindlegen.exe");
		String cmdStr = String.format(kindlegenPath + " %s -locale zh", page.getSavePath());
		Process process;
		process = Runtime.getRuntime().exec(cmdStr);
		try {
			// process.waitFor();
			String result = new String(IOUtils.read(process.getInputStream()));
			LOGGER.debug("kindlegen output info:" + result);
			sendToKindle(page);
		} catch (Exception e) {
			LOGGER.error("kindlegen error.", e);
		}
	}

	private void sendToKindle(PageEntry page) {
		MailSender mailSender = new MailSender(GlobalConfig.CONFIGS);
		mailSender.sendFrom(page.getTitle(), page.getMobiFilePath());
		LOGGER.debug("sended mobi file to：" + GlobalConfig.CONFIGS.getProperty("mail.to"));
	}

	protected void processImages(PageEntry page) {
		StringBuilder processed = new StringBuilder();
		StringBuilder imgElement = new StringBuilder();
		StringBuilder content = page.getContent();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (index < 4 && c == IMG_START_TAG[index]) {
				index++;
			} else {
				index = 0;
			}
			if (index == 4) {
				processed.delete(processed.length() - 3, processed.length());
				imgElement.append("<img");
				index = 0;
				while (true) {
					c = content.charAt(++i);
					imgElement.append(c);
					if (index < 2 && c == IMG_END_TAG[index]) {
						index++;
					} else {
						index = 0;
					}
					if (index == 2) {
						index = 0;
						break;
					}
				}
				processed.append(downloadImage(imgElement.toString()));
				imgElement.delete(0, imgElement.length());
			} else {
				processed.append(c);
			}
			page.setContent(processed);
		}
	}

	private String downloadImage(String imgElement) {
		List<String> matchs = RegexUtils.findAll("(?<=src=\").*?(?=\")", imgElement, false);
		if (matchs.isEmpty()) {
			return imgElement;
		}
		String url = matchs.get(0);

		final String fileName = getImageFileName(url);
		final String result = RegexUtils.replaceAll("(?<=src=\").*?(?=\")", imgElement, "images/" + fileName, false);
		final ImageEntry img = new ImageEntry(fileName, url, page.getImgDir() + fileName);
		FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try (OutputStream os = new FileOutputStream(img.getSavePath())) {
					HttpHelper.download(img.getDownloadUrl(), GlobalConfig.DOWNLOAD_TIMEOUT, os);
					LOGGER.debug("downloaded image:" + img.toString());
				} catch (Exception e) {
					LOGGER.error("download image error:" + img.getDownloadUrl(), e);
				}
				return true;
			}
		});
		downloaders.submit(task);
		futureTasks.add(task);
		return result;
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

	private String getImageFileName(String url) {
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
