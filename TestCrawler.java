import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestCrawler {
    public static void main(String[] args) {
        try {
            String url = "https://www.travelking.com.tw/tourguide/taiwan/taipei-city";
            System.out.println("正在抓取：" + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            System.out.println("\n========== 頁面標題 ==========");
            System.out.println(doc.title());

            System.out.println("\n========== 尋找 div.box ==========");
            Elements boxDivs = doc.select("div.box");
            System.out.println("找到 " + boxDivs.size() + " 個 div.box");

            if (boxDivs.isEmpty()) {
                System.out.println("\n========== 列出所有 div 的 class ==========");
                Elements allDivs = doc.select("div[class]");
                for (int i = 0; i < Math.min(20, allDivs.size()); i++) {
                    Element div = allDivs.get(i);
                    System.out.println("div class=\"" + div.className() + "\"");
                }
            }

            System.out.println("\n========== 尋找所有包含 scenery 的連結 ==========");
            Elements sceneryLinks = doc.select("a[href*=scenery]");
            System.out.println("找到 " + sceneryLinks.size() + " 個景點連結");

            for (int i = 0; i < Math.min(5, sceneryLinks.size()); i++) {
                Element link = sceneryLinks.get(i);
                System.out.println(link.text() + " -> " + link.attr("href"));
            }

            System.out.println("\n========== 頁面 HTML 結構（前 2000 字） ==========");
            String html = doc.html();
            System.out.println(html.substring(0, Math.min(2000, html.length())));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
