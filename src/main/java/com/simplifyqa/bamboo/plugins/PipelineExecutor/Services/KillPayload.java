package com.simplifyqa.bamboo.plugins.PipelineExecutor.Services;

public class KillPayload {
    int customerId, userId;
    long id;
    String userName;

    // Constructor
    KillPayload(int customerId, long id, int userId, String userName) {
        this.customerId = customerId;
        this.id = id;
        this.userId = userId;
        this.userName = userName;
    }

    // Getters
    public String getPayload() {
        StringBuilder sb = new StringBuilder("");

        sb.append("{\"customerId\":" + this.customerId + ",");
        sb.append("\"id\":" + this.id + ",");
        sb.append("\"userId\":" + this.userId + ",");
        sb.append("\"userName\":\"" + this.userName + "\"}");

        return sb.toString();
    }

    public int getCustomerId() {
        return customerId;
    }

    public long getId() {
        return this.id;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    // Setters
    protected void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    protected void setId(long id) {
        this.id = id;
    }

    protected void setUserId(int userId) {
        this.userId = userId;
    }

    protected void setUserName(String userName) {
        this.userName = userName;
    }
}
