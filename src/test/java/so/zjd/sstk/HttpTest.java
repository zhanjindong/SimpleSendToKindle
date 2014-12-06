package so.zjd.sstk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import so.zjd.sstk.util.HttpHelper;

public class HttpTest {
	public static void main(String[] args) throws IOException {
		String url = "http://www.cnblogs.com/magialmoon/p/3588755.html";
		String content = HttpHelper.download(url, 5000, "utf-8").toString();
		System.out.println(content);
		
		String imgurl = "http://zhanjindong.info/wp-content/uploads/2014/03/c-result.png";
		OutputStream os = new FileOutputStream("d://c-result.png");
		HttpHelper.download(imgurl, 5000, "utf-8",os);
	}
}
