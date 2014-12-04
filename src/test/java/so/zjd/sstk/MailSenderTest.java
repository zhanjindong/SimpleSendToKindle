package so.zjd.sstk;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;

import so.zjd.sstk.util.MailSender;
import so.zjd.sstk.util.PathUtils;

public class MailSenderTest {

	private static final String WORK_DIR = PathUtils.getAppDir(Service.class);
	private static final Properties CONFIGS = new Properties();

	static {
		try {
			DOMConfigurator.configure(PathUtils.getRealPath("classpath:log4j.xml"));
			CONFIGS.load(new FileInputStream(PathUtils.getRealPath("classpath:sstk.properties")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MailSender sender = new MailSender(CONFIGS);
		sender.sendFrom("simple send to kindle","d:\\error");
	}
}
