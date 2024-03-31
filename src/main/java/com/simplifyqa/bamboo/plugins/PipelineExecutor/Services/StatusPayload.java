package io.jenkins.plugins.simplifyqa.Services;

public class StatusPayload {
    long executionId;
    int customerId, projectId;

    // Constructor
    public StatusPayload(long executionId, int customerId, int projectId) {
        this.executionId = executionId;
        this.customerId = customerId;
        this.projectId = projectId;
    }

    // Getters
    public String getPayload() {
        StringBuilder sb = new StringBuilder("");
        sb.append("{\"executionId\":" + String.valueOf(this.executionId) + ",");
        sb.append("\"customerId\":" + this.customerId + ",");
        sb.append("\"projectId\":" + this.projectId + "}");

        return sb.toString();
    }

    public long getExecutionId() {
        return this.executionId;
    }

    public int getCustomerId() {
        return this.customerId;
    }

    public int getProjectId() {
        return this.projectId;
    }

    // Setters
    protected void setExecutionId(long executionId) {
        this.executionId = executionId;
    }

    protected void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    protected void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
