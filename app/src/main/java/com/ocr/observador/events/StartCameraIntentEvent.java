package com.ocr.observador.events;

/**
 * Created by Vazh on 5/5/2015.
 */
public class StartCameraIntentEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    boolean _isImage;

    private int _resultCode;

    private int _categoryNumber;

    public StartCameraIntentEvent(Enum type, boolean _isImage, int _resultCode, int _categoryNumber) {
        super(type);
        this._isImage = _isImage;
        this._resultCode = _resultCode;
        this._categoryNumber = _categoryNumber;
    }

    public boolean isImage() {
        return _isImage;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public int getCategoryNumber() {
        return _categoryNumber;
    }
}
