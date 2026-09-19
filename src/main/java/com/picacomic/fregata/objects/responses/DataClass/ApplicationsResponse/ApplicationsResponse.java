package com.picacomic.fregata.objects.responses.DataClass.ApplicationsResponse;

/**
 * 应用列表响应。
 * Applications response.
 */
public class ApplicationsResponse {
    ApplicationsData applications;

    public ApplicationsResponse(ApplicationsData applications) {
        this.applications = applications;
    }

    public ApplicationsData getApplications() {
        return this.applications;
    }

    public void setApplications(ApplicationsData applications) {
        this.applications = applications;
    }

    public String toString() {
        return "ApplicationsResponse{applications=" + this.applications + '}';
    }
}
