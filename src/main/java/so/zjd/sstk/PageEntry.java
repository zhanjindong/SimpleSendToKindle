package so.zjd.sstk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;

import so.zjd.sstk.util.HttpHelper;
import so.zjd.sstk.util.RegexUtils;
import so.zjd.sstk.util.SeparatorUtils;

public class PageEntry implements AutoCloseable {

	private static final String SLASH = SeparatorUtils.getFileSeparator();
	private String url;
	private StringBuilder content;
	private String title;
	private String tmpDir;
	private String imgDir;
	private String savePath;

	public PageEntry(String url) {
		this.url = url;
		prepare();
	}

	private void prepare() {
		try {
			this.content = HttpHelper.download(url, GlobalConfig.DOWNLOAD_TIMEOUT);
			Path path = Paths.get(GlobalConfig.BASE_TEMP_DIR);
			if (!Files.exists(path)) {
				Files.createDirectory(path);
			}

			this.tmpDir = GlobalConfig.BASE_TEMP_DIR + SLASH + clean(url);
			this.imgDir = tmpDir + SLASH + "images" + SLASH;
			path = Paths.get(tmpDir);
			if (Files.exists(path)) {
				delete(path);
			}
			Files.createDirectory(path);
			path = new File(GlobalConfig.BASE_TEMP_DIR + SLASH + clean(url) + "/images").toPath();
			Files.createDirectory(path);

			this.title = RegexUtils.findAll("(?<=<title>).*?(?=</title>)", this.content.toString(), false).get(0);
			if (StringUtils.isEmpty(this.title)) {
				this.title = "Unknow";
			}
			this.savePath = this.tmpDir + SLASH + this.title + ".html";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String clean(String url) {
		return url.replace("http://", "").replace("/", ".").replace("?", "");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public StringBuilder getContent() {
		return content;
	}

	public void setContent(StringBuilder content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public void close() {
		if (GlobalConfig.DELETE_TEMP_DIR) {
			delete(tmpDir);
		}
	}

	public String getImgDir() {
		return imgDir;
	}

	public void setImgDir(String imgDir) {
		this.imgDir = imgDir;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public void delete(Path path) {
		delete(path.toString());
	}

	public void delete(String path) {
		File f = new File(path);
		if (f.isDirectory()) {
			String[] list = f.list();
			for (int i = 0; i < list.length; i++) {
				delete(path + "//" + list[i]);
			}
		}
		f.delete();
	}

	@Override
	public String toString() {
		return "PageEntry [url=" + url + ", title=" + title + ", tmpDir=" + tmpDir + ", savePath=" + savePath + "]";
	}

}
