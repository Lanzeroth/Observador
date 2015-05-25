package com.ocr.observador.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 15/5/2015.
 */
@Table(name = "ModelMarkers")
public class ModelMarker extends Model {

    @Column
    public int backendId;

    @Column
    public String title;

    @Column
    public String content;

    @Column
    public double latitude;

    @Column
    public double longitude;

    public ModelMarker(int backendId, String title, String content, double latitude, double longitude) {
        this.backendId = backendId;
        this.title = title;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ModelMarker() {
        super();
    }
}
