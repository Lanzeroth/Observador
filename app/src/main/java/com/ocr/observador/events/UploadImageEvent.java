package com.ocr.observador.events;

/**
 * Created by Vazh on 5/5/2015.
 */
public class UploadImageEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;
    private String _imageName;
    private int _category;

    public UploadImageEvent(Enum type, int _resultCode, String _imageName, int _category) {
        super(type);
        this._resultCode = _resultCode;
        this._imageName = _imageName;
        this._category = _category;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public String getImageName() {
        return _imageName;
    }

    public int getCategory() {
        return _category;
    }
}
