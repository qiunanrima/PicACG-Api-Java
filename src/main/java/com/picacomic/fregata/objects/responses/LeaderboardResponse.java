package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.LeaderboardComicListObject;
import java.util.ArrayList;

/**
 * 排行榜响应。
 * Leaderboard response.
 */
public class LeaderboardResponse {
    ArrayList<LeaderboardComicListObject> comics;

    public LeaderboardResponse(ArrayList<LeaderboardComicListObject> comics) {
        this.comics = comics;
    }

    public ArrayList<LeaderboardComicListObject> getComics() {
        return this.comics;
    }

    public void setComics(ArrayList<LeaderboardComicListObject> comics) {
        this.comics = comics;
    }

    public String toString() {
        return "LeaderboardResponse{comics=" + this.comics + '}';
    }
}
