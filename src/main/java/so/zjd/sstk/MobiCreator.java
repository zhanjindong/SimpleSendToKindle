package so.zjd.sstk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobiCreator {

	private static ExecutorService imgDownloaders = Executors.newFixedThreadPool(2);
	private static List<String> supportedImgFormats = new ArrayList<String>();

	static {
		supportedImgFormats.add(".JPEG");
		supportedImgFormats.add(".JPG");
		supportedImgFormats.add(".GIF");
		supportedImgFormats.add(".PNG");
		supportedImgFormats.add(".BMP");
	}
	private PageEntry page;

	public MobiCreator(PageEntry page) {
		this.page = page;
	}

	public void create() {
		System.out.println(page.getContent().toString().length());
	}
}
