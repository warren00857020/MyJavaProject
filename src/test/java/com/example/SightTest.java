package com.example;


import com.example.entity.Sight;
import com.example.exception.NotFoundException;
import com.example.repository.SightRepository;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


//定義測試程式要在 Spring Boot 的環境下執行
@RunWith(SpringRunner.class)
@SpringBootTest

//代表測試開始時會在元件容器中建立 MockMvc 元件
@AutoConfigureMockMvc
public class SightTest {
    private HttpHeaders httpHeaders;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SightRepository sightRepository;

    @Before
    public void init(){
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    //測試完清掉資料
    @After
    public void clear(){
        sightRepository.deleteAll();
    }

    //產生測試sight
    private Sight createSight (String sightName, String zone, String category, String photoURL, String description, String address){
        Sight sight = new Sight();
        sight.setSightName(sightName);
        sight.setZone(zone);
        sight.setCategory(category);
        sight.setPhotoURL(photoURL);
        sight.setDescription(description);
        sight.setAddress(address);

        return sight;
    }

    //新增測試
    @Test
    public void testCreateSight() throws Exception{
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        JSONObject request = new JSONObject()
                .put("sightName","sightName_test")
                .put("zone","zone_test")
                .put("category","category_test")
                .put("photoURL","photoURL_test")
                .put("description","description_test")
                .put("address","address_test");

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/sights")
                .headers(httpHeaders)
                .content(request.toString());
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sightName").value(request.getString("sightName")))
                .andExpect(jsonPath("$.zone").value(request.getString("zone")))
                .andExpect(jsonPath("$.category").value(request.getString("category")))
                .andExpect(jsonPath("$.photoURL").value(request.getString("photoURL")))
                .andExpect(jsonPath("$.description").value(request.getString("description")))
                .andExpect(jsonPath("$.address").value(request.getString("address")))
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    //查詢sight測試
    @Test
    public void testGetSight() throws Exception{
        Sight sight = createSight("sightName_searchTest","zone_searchTest","category_searchTest","photoURL_searchTest","description_searchTest","address_searchTest");
        sightRepository.insert(sight);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/sights/"+sight.getSightName())
                .headers(httpHeaders);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sightName").value(sight.getSightName()))
                .andExpect(jsonPath("$.zone").value(sight.getZone()))
                .andExpect(jsonPath("$.category").value(sight.getCategory()))
                .andExpect(jsonPath("$.photoURL").value(sight.getPhotoURL()))
                .andExpect(jsonPath("$.description").value(sight.getDescription()))
                .andExpect(jsonPath("$.address").value(sight.getAddress()));
    }

    //查詢sight by zone 測試
    @Test
    public void testGetSightByZone() throws Exception{
        Sight sight1 = createSight("sightName_zoneTest1","zone_zoneTest1","category_zoneTest1","photoURL_zoneTest1","description_zoneTest1","address_zoneTest1");
        Sight sight2 = createSight("sightName_zoneTest2","zone_zoneTest2","category_zoneTest2","photoURL_zoneTest2","description_zoneTest2","address_zoneTest2");
        Sight sight3 = createSight("sightName_zoneTest3","zone_zoneTest1","category_zoneTest3","photoURL_zoneTest3","description_zoneTest3","address_zoneTest3");
        sightRepository.insert(Arrays.asList(sight1,sight2,sight3));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/sights")
                .headers(httpHeaders)
                .param("keyword","zone_zoneTest1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].sightName").value(sight1.getSightName()))
                .andExpect(jsonPath("$[1].sightName").value(sight3.getSightName()));

    }

    //刪除sight測試
    @Test(expected = RuntimeException.class)
    public void testDeleteSight() throws Exception{
        Sight sight = createSight("sightName_deleteTest","zone_deleteTest","category_deleteTest","photoURL_deleteTest","description_deleteTest","address_deleteTest");
        sightRepository.insert(sight);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/sights/"+sight.getSightName())
                .headers(httpHeaders);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

        Optional<Sight> optionalSight = Optional.ofNullable(sightRepository.findBySightName(sight.getSightName()));
        if (optionalSight.isEmpty()){
            throw new RuntimeException();
        }
    }

    @Test
    public void get400WhenCreateSightWithEmptyName() throws Exception{
        JSONObject request = new JSONObject()
                .put("sightName","")
                .put("zone","zone_badTest")
                .put("category","category_badTest")
                .put("photoURL","photoURL_badTest")
                .put("description","description_badTest")
                .put("address","address_badTest");

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/sights")
                .headers(httpHeaders)
                .content(request.toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());

    }
}
