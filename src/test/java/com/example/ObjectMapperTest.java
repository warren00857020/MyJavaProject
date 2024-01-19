package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTest {
    private ObjectMapper mapper = new ObjectMapper();

    private static class Park{
        private String id;
        private String name;
        private String address;
        private int area;
        private ManageAgent manager;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public int getArea() {
            return area;
        }

        public ManageAgent getManager() {
            return manager;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setArea(int area) {
            this.area = area;
        }

        public void setManager(ManageAgent manager) {
            this.manager = manager;
        }
    }

    private static class ManageAgent{
        private String agentName;
        private String Manager;

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public String getManager() {
            return Manager;
        }

        public void setManager(String manager) {
            Manager = manager;
        }
    }
}
