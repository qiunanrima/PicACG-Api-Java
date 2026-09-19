package com.picacomic.fregata.objects.responses.DataClass.AnnouncementsResponse;

/**
 * 公告列表响应。
 * Announcements response.
 */
public class AnnouncementsResponse {
    AnnouncementsData announcements;

    public AnnouncementsResponse(AnnouncementsData announcements) {
        this.announcements = announcements;
    }

    public AnnouncementsData getAnnouncements() {
        return this.announcements;
    }

    public void setAnnouncements(AnnouncementsData announcements) {
        this.announcements = announcements;
    }
}
