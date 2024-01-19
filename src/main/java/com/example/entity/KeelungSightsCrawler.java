package com.example.entity;

import com.example.service.SightService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeelungSightsCrawler {
    private String url = "https://www.travelking.com.tw/tourguide/taiwan/keelungcity/" ;
    private Elements area ;
    private Elements allSight ;
    public KeelungSightsCrawler() throws IOException {
        String htmlPage = Jsoup.connect(url).get().toString();
        Document doc = Jsoup.parse(htmlPage) ;
        Elements content = doc.getElementsByClass("tourgudes fold");
        Document doc2 = Jsoup.parse(String.valueOf(content));
        area = doc2.select("h4");
        allSight = doc2.select("h4~ul"); //所有觀光景點 一個ul下代表一個區的觀光景點
    }
    public List<SightRequest> getItems(String city) throws IOException {
        int i = 0;

        //查輸入的行政區在我們爬回來資料的第幾個
        for(Element element : area){
            if(city.equals(element.text())){
                break;
            }
            i+=1;
        }

        //把輸入的行政區的觀光景點整理出來
        Document doc3 = Jsoup.parse(String.valueOf(allSight.get(i)));
        Elements areaSight = doc3.select("a");
//        System.out.println(areaSight);

        //把觀光景點一一處理
        List<SightRequest>sights = new ArrayList<>();
        for (Element element : areaSight) {
//            System.out.println(element.text());//
            SightRequest sight = new SightRequest();

            //setSightName
            sight.setSightName(element.text());

            //setZone
            sight.setZone(city);


            String sightURL = "https://www.travelking.com.tw"+element.attr("href");
//            System.out.println(sightURL);//
            String htmlPage = Jsoup.connect(sightURL).get().toString();
            Document doc4 = Jsoup.parse(htmlPage) ;

            //setCategory
            Elements categoryContent = doc4.getElementsByClass("point_type");
            Document doc5 = Jsoup.parse(String.valueOf(categoryContent));
//            System.out.println(doc5.select("strong").text());
            sight.setCategory(doc5.select("strong").text());

            //setPhotoURL
            try{
                Element photoContent = doc4.select("div.gpic").first();
                Document doc2 = Jsoup.parse(String.valueOf(photoContent));
                Element e = doc2.select("img").first();
//                System.out.println(e.attr("data-src"));
                //setPhotoURL
                sight.setPhotoURL(e.attr("data-src"));
            } catch (Exception e){
                sight.setPhotoURL(null);
            }

            //setDescription
            Elements descriptionContent = doc4.select("#point_area > div.text");
//            System.out.println(descriptionContent.text());
            sight.setDescription(descriptionContent.text());

            //setAddress
            Elements addressContent = doc4.select("#point_data > div.address > p > a > span");
//            System.out.println(addressContent.text());
            sight.setAddress(addressContent.text());

//            new SightService().createSight(sight);
            sights.add(sight);
        }

        return sights;
    }
}
