package so.zjd.sstk;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import so.zjd.sstk.util.PathUtils;
import so.zjd.sstk.util.SeparatorUtils;

public class GlobalConfig {
	public static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
	public static final String WORK_DIR = PathUtils.getAppDir(GlobalConfig.class);
	public static final Properties CONFIGS = new Properties();
	public static final String BASE_TEMP_DIR = WORK_DIR + "\\temp";
	public static final String SLASH = SeparatorUtils.getFileSeparator();;

	public static int SERVICE_TIMEOUT = 0;
	public static int DOWNLOAD_TIMEOUT = 0;
	public static boolean DELETE_TEMP_DIR = false;

	static {
		try {
			DOMConfigurator.configure(PathUtils.getRealPath("classpath:log4j.xml"));
			CONFIGS.load(new FileInputStream(PathUtils.getRealPath("classpath:sstk.properties")));

			SERVICE_TIMEOUT = Integer.valueOf(CONFIGS.getProperty("sstk.service.timeout"));
			DOWNLOAD_TIMEOUT = Integer.valueOf(CONFIGS.getProperty("sstk.download.timeout"));
			DELETE_TEMP_DIR = Boolean.valueOf(CONFIGS.getProperty("sstk.download.deleteTmpDir"));

		} catch (Exception e) {
			LOGGER.error("static init error.", e);
		}
	}

	public static void init() {
		// do nothing
	}
}
