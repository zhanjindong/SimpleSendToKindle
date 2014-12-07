package so.zjd.sstk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;

import so.zjd.sstk.util.HttpHelper;
import so.zjd.sstk.util.IOUtils;
import so.zjd.sstk.util.RegexUtils;

/**
 * 
 * Represents of web pages.
 * 
 * @author jdzhan,2014-12-6
 * 
 */
public class PageEntry implements AutoCloseable {

	private String url;
	private StringBuilder content;
	private String title;
	private String tmpDir;
	private String resourceDir;
	private String savePath;
	private String mobiFilePath;

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

			this.tmpDir = GlobalConfig.BASE_TEMP_DIR + GlobalConfig.SLASH + normalizePath(url);
			this.resourceDir = tmpDir + GlobalConfig.SLASH + GlobalConfig.RESOURCE_DIR_NAME + GlobalConfig.SLASH;
			path = Paths.get(tmpDir);
			if (Files.exists(path)) {
				delete(path);
			}
			Files.createDirectory(path);
			path = new File(GlobalConfig.BASE_TEMP_DIR + GlobalConfig.SLASH + normalizePath(url) + GlobalConfig.SLASH
					+ GlobalConfig.RESOURCE_DIR_NAME).toPath();
			Files.createDirectory(path);

			String tmpTitle = RegexUtils.findAll("(?<=<title>).*?(?=</title>)", this.content.toString(), false).get(0);
			if (StringUtils.isEmpty(tmpTitle)) {
				this.title = "Unknow";
			} else {
				this.title = normalizePath(tmpTitle);
			}

			this.savePath = this.tmpDir + GlobalConfig.SLASH + this.title + ".html";
			this.mobiFilePath = this.tmpDir + GlobalConfig.SLASH + this.title + ".mobi";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String normalizePath(String url) {
		return url.trim().replace("http://", "").replace("/", ".").replace("?", "").replace(" ", "-")
				.replace("&nbsp;", "");
	}

	public void save() throws IOException {
		IOUtils.write(savePath, content.toString(), true);
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
		return resourceDir;
	}

	public void setImgDir(String imgDir) {
		this.resourceDir = imgDir;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getMobiFilePath() {
		return mobiFilePath;
	}

	public void setMobiFilePath(String mobiFilePath) {
		this.mobiFilePath = mobiFilePath;
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
