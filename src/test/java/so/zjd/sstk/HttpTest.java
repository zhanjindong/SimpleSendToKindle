package so.zjd.sstk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import so.zjd.sstk.util.HttpHelper;
import so.zjd.sstk.util.RegexUtils;

public class HttpTest {
	public static void main(String[] args) throws IOException {
		String url = "http://www.cnblogs.com/guogangj/p/3235703.html";
		String content = HttpHelper.download(url, 5000, "utf-8").toString();
		System.out.println(content);

		OutputStream os = new FileOutputStream("d://test.html");
		//HttpHelper.download(url, 5000, "utf-8", os);

		StringBuilder sb = HttpHelper.download(url, 1000);
		System.out.println(sb.toString());
		os.write(sb.toString().getBytes("UTF-8"));
		
	}
}
