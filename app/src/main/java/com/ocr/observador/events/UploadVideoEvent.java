package com.ocr.observador.events;

/**
 * Created by Vazh on 5/5/2015.
 */
public class UploadVideoEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;
    private String _videoName;
    private int _category;

    public UploadVideoEvent(Enum type, int _resultCode, String _videoName, int _category) {
        super(type);
        this._resultCode = _resultCode;
        this._videoName = _videoName;
        this._category = _category;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public String getVideoName() {
        return _videoName;
    }

    public int getCategory() {
        return _category;
    }
}
