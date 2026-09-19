package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.BannerObject;
import java.util.ArrayList;

/**
 * 轮播图响应。
 * Banners response.
 */
public class BannersResponse {
    ArrayList<BannerObject> banners;

    public BannersResponse(ArrayList<BannerObject> banners) {
        this.banners = banners;
    }

    public ArrayList<BannerObject> getBanners() {
        return this.banners;
    }

    public void setBanners(ArrayList<BannerObject> banners) {
        this.banners = banners;
    }
}
