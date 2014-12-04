package so.zjd.sstk;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import so.zjd.sstk.util.PathUtils;

public class GlobalConfig {
	public static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
	public static final String WORK_DIR = PathUtils.getAppDir(GlobalConfig.class);
	public static final Properties CONFIGS = new Properties();
	public static final String BASE_TEMP_DIR = WORK_DIR + "\\temp";

	static {
		try {
			DOMConfigurator.configure(PathUtils.getRealPath("classpath:log4j.xml"));
			CONFIGS.load(new FileInputStream(PathUtils.getRealPath("classpath:sstk.properties")));
		} catch (Exception e) {
			LOGGER.error("static init error.", e);
		}
	}

	public static void init() {
		// do nothing
	}
}
