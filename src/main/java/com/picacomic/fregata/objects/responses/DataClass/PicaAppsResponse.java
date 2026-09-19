package com.picacomic.fregata.objects.responses.DataClass;

import com.picacomic.fregata.objects.PicaAppObject;
import java.util.ArrayList;

/**
 * Pica
 * Pica apps response.
 */
public class PicaAppsResponse {
    ArrayList<PicaAppObject> apps;

    public PicaAppsResponse(ArrayList<PicaAppObject> apps) {
        this.apps = apps;
    }

    public ArrayList<PicaAppObject> getApps() {
        return this.apps;
    }

    public void setApps(ArrayList<PicaAppObject> apps) {
        this.apps = apps;
    }

    public String toString() {
        return "PicaAppsResponse{apps=" + this.apps + '}';
    }
}
