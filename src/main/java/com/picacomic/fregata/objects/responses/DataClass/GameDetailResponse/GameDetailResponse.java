package com.picacomic.fregata.objects.responses.DataClass.GameDetailResponse;

import com.picacomic.fregata.objects.GameDetailObject;

/**
 * 游戏详情响应。
 * Game detail response.
 */
public class GameDetailResponse {
    GameDetailObject game;

    public GameDetailResponse(GameDetailObject game) {
        this.game = game;
    }

    public GameDetailObject getGame() {
        return this.game;
    }

    public void setGame(GameDetailObject game) {
        this.game = game;
    }

    public String toString() {
        return "GameDetailResponse{game=" + this.game + '}';
    }
}
