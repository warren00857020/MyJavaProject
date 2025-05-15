//package com.example;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.annotation.JsonUnwrapped;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class ObjectMapperTest {
//    private ObjectMapper mapper = new ObjectMapper();
//
//    private static class Park{
//        private String id;
//        private String name;
//        private String address;
//        private int area;
//        @JsonUnwrapped
//        private ManageAgent manager;
//
//        public String getId() {
//            return id;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getAddress() {
//            return address;
//        }
//
//        public int getArea() {
//            return area;
//        }
//
//        public ManageAgent getManager() {
//            return manager;
//        }
//
//        public void setId(String id) {
//            this.id = id;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public void setAddress(String address) {
//            this.address = address;
//        }
//
//        public void setArea(int area) {
//            this.area = area;
//        }
//
//        public void setManager(ManageAgent manager) {
//            this.manager = manager;
//        }
//    }
//
//    private static class ManageAgent{
//        private String agentName;
//        @JsonProperty("managerName")
//        private String manager;
//
//        public String getAgentName() {
//            return agentName;
//        }
//
//        public void setAgentName(String agentName) {
//            this.agentName = agentName;
//        }
//
//        public String getManager() {
//            return manager;
//        }
//
//        public void setManager(String manager) {
//            this.manager = manager;
//        }
//    }
//
//    //Park 跟 ManageAgent都屬於 ObjectMapperTest的內部類別，需要宣告為static，否則 Jackson 無法進行反序列化
//    @Test
//    public void testSerializeParkToJSON() throws Exception{
//        Park park = new Park();
//        park.setId("P001");
//        park.setArea(521);
//        park.setName("Keelung Park");
//        park.setAddress("Xinyi Street");
//
//        String parkJSONStr = mapper.writeValueAsString(park);
//        JSONObject parkJSON = new JSONObject(parkJSONStr);
//
//        Assert.assertEquals(park.getId(),parkJSON.getString("id"));
//        Assert.assertEquals(park.getName(),parkJSON.getString("name"));
//        Assert.assertEquals(park.getArea(),parkJSON.getInt("area"));
//        Assert.assertEquals(park.getAddress(),parkJSON.getString("address"));
//    }
//
//    @Test
//    public void testDeserializeJSONToManageAgent() throws Exception{
//        JSONObject manageAgentJSON = new JSONObject()
//                .put("agentName","Keelung government")
//                .put("manager","陳威融");
//
//        String manageAgentJSONStr = manageAgentJSON.toString();
//        ManageAgent manageAgent = mapper.readValue(manageAgentJSONStr,ManageAgent.class);
//
//        Assert.assertEquals(manageAgentJSON.getString("agentName"),manageAgent.getAgentName());
//        Assert.assertEquals(manageAgentJSON.getString("manager"),manageAgent.getManager());
//    }
//}
