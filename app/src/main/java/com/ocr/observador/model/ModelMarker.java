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
    public String address;

    @Column
    public String district;

    @Column
    public String national_id;

    @Column
    public String title;

    @Column
    public String url_key;

    @Column
    public double latitude;

    @Column
    public double longitude;

    public ModelMarker(String address, String district, String national_id, String title, String url_key, double latitude, double longitude) {
        this.address = address;
        this.district = district;
        this.national_id = national_id;
        this.title = title;
        this.url_key = url_key;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ModelMarker() {
        super();
    }
}
