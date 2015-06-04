package com.ocr.observador.events;

/**
 * Created by Vazh on 25/5/2015.
 */
public class MarkerClickedEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    private int _markerId;


    public MarkerClickedEvent(Enum type, int _resultCode, int _markerId) {
        super(type);
        this._resultCode = _resultCode;
        this._markerId = _markerId;
    }

    public int getMarkerId() {
        return _markerId;
    }

    public int getResultCode() {
        return _resultCode;
    }

}
