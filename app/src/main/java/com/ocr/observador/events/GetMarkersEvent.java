package com.ocr.observador.events;

/**
 * Created by Vazh on 25/5/2015.
 */
public class GetMarkersEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    public GetMarkersEvent(Type type, int resultCode) {
        super(type);
        this._resultCode = resultCode;

    }

    public int getResultCode() {
        return _resultCode;
    }

}
