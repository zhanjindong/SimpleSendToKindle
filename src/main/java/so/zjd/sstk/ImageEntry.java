package so.zjd.sstk;

import java.util.ArrayList;
import java.util.List;

public class ImageEntry {
	private String downloadUrl;
	private String savePath;
	private String fileName;

	private static List<String> supportedImgFormats = new ArrayList<String>();

	static {
		supportedImgFormats.add(".JPEG");
		supportedImgFormats.add(".JPG");
		supportedImgFormats.add(".GIF");
		supportedImgFormats.add(".PNG");
		supportedImgFormats.add(".BMP");
	}

	public ImageEntry(String fileName,String downloadUrl, String savePath) {
		this.fileName = fileName;
		this.downloadUrl = downloadUrl;
		this.savePath = savePath;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "ImageEntry [downloadUrl=" + downloadUrl + ", savePath=" + savePath + "]";
	}
}
