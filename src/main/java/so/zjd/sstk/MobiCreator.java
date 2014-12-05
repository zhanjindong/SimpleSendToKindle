package so.zjd.sstk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobiCreator {

	private static final char[] IMG_START_TAG = new char[] { '<', 'i', 'm', 'g' };
	private static final char[] IMG_END_TAG = new char[] { '/', '>' };

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
	private int index = 0;

	public MobiCreator(PageEntry page) {
		this.page = page;
	}

	public void create() {
		processImages(page);
	}

	protected void processImages(PageEntry page) {
		StringBuilder processed = new StringBuilder();
		StringBuilder imgElement = new StringBuilder();
		StringBuilder content = page.getContent();
		// content = new
		// StringBuilder("<p><span><img style=\"border: 1px solid black;\" src=\"http://images.cnitblog.com/blog/379997/201308/03231707-438355177e8940849c93a76bcff24ece.png\" alt=\"\" /></span></p>");
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
				System.out.println("img element:" + imgElement.toString());
				downloadImg(imgElement);
				processed.append(imgElement.toString());
				imgElement.delete(0, imgElement.length());
			} else {
				processed.append(c);
			}
		}
		System.out.println(processed.toString().equals(content.toString()));
		System.out.println();
	}
	
	private void downloadImg(StringBuilder imgElement){
	}

}
