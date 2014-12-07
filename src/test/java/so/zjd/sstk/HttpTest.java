package so.zjd.sstk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import so.zjd.sstk.util.HttpHelper;
import so.zjd.sstk.util.RegexUtils;

public class HttpTest {
	public static void main(String[] args) throws IOException {
		String url = "http://wmljava.iteye.com/blog/1846252";
		String content = HttpHelper.download(url, 5000, "utf-8").toString();
		System.out.println(content);
		
//		String imgurl = "http://cms.csdnimg.cn/article/201412/03/547e6167d719e_middle.jpg?_=22960";
//		System.out.println(imgurl);
//		OutputStream os = new FileOutputStream("d://test.png");
//		HttpHelper.download(imgurl, 5000, "utf-8",os);
	}
}
