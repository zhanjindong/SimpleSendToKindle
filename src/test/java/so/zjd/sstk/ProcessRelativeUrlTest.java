package so.zjd.sstk;

import junit.framework.TestCase;

public class ProcessRelativeUrlTest extends TestCase {
	public static void main(String[] args) {
		String result = processRelativeUrl("http://redisbook1e.readthedocs.org/en/latest/preview/slowlog/content.html",
				".././../_images/graphviz-496accb4258b0feb9fbc0503bb9ba49f16e4b6d9.png");
		System.out.println(result);
	}

	public void testRelativeUrl() {
		String pageUrl = "http://www.test.com/dir1/dir2/dir3/test.html";

		String imgUrl = "../test.png";
		String result = processRelativeUrl(pageUrl, imgUrl);
		assertEquals("http://www.test.com/dir1/dir2/test.png", result);

		imgUrl = "./test.png";
		result = processRelativeUrl(pageUrl, imgUrl);
		assertEquals("http://www.test.com/dir1/dir2/dir3/test.png", result);

		imgUrl = "/test.png";
		result = processRelativeUrl(pageUrl, imgUrl);
		assertEquals("http://www.test.com/test.png", result);
		
		imgUrl = "./../test.png";
		 result = processRelativeUrl(pageUrl, imgUrl);
		assertEquals("http://www.test.com/dir1/dir2/test.png", result);
		
		imgUrl = "../../test.png";
		 result = processRelativeUrl(pageUrl, imgUrl);
		assertEquals("http://www.test.com/dir1/test.png", result);
	}

	// ./images/mem/figure9.png
	// images/mem/figure9.png
	// /images/mem/figure9.png
	// ../../images/mem/figure9.png
	// page url:http://www.test.com/dir1/dir2/test.html
	private static String processRelativeUrl(String pageUrl, String url) {
		if (url.startsWith("http://")) {
			return url;
		}
		int relative = 0;
		int index = 0;
		if (url.startsWith("/")) {
			relative = -1;
		} else {
			while (true) {
				index = 0;
				if (url.startsWith("./")) {// 当前目录
					index = url.indexOf("./");
					url = url.substring(index + 2);
					continue;
				} else if (url.startsWith("../")) {// 上级目录
					relative++;
					index = url.indexOf("../");
					url = url.substring(index + 3);
					continue;
				} else {// 当前目录
					break;
				}
			}
		}
		if (relative == -1) {
			index = pageUrl.indexOf('/', 7);
			pageUrl = pageUrl.substring(0, index);
			url = url.substring(1);
		} else {
			for (int i = 0; i <= relative; i++) {
				index = pageUrl.lastIndexOf("/");
				if (index == -1) {
					break;
				}
				pageUrl = pageUrl.substring(0, index);
			}
		}
		url = pageUrl + "/" + url;

		return url;
	}
}
