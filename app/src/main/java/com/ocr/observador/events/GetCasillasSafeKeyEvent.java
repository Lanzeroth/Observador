package com.ocr.observador.events;

/**
 * Created by Vazh on 25/5/2015.
 */
public class GetCasillasSafeKeyEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;


    public GetCasillasSafeKeyEvent(Enum type, int _resultCode) {
        super(type);
        this._resultCode = _resultCode;
    }


    public int getResultCode() {
        return _resultCode;
    }

}
