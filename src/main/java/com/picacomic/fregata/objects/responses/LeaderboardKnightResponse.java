package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.LeaderboardKnightObject;
import java.util.ArrayList;

/**
 * 骑士榜响应。
 * Knight leaderboard response.
 */
public class LeaderboardKnightResponse {
    ArrayList<LeaderboardKnightObject> users;

    public LeaderboardKnightResponse(ArrayList<LeaderboardKnightObject> users) {
        this.users = users;
    }

    public ArrayList<LeaderboardKnightObject> getUsers() {
        return this.users;
    }

    public void setUsers(ArrayList<LeaderboardKnightObject> users) {
        this.users = users;
    }

    public String toString() {
        return "LeaderboardKnightResponse{users=" + this.users + '}';
    }
}
