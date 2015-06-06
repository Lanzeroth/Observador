package com.ocr.observador.events;


/**
 * Created by Vazh on 5/5/2015.
 */
public class StartRegisteringUserEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    public StartRegisteringUserEvent(Type type, int resultCode) {
        super(type);
        this._resultCode = resultCode;

    }

    public int getResultCode() {
        return _resultCode;
    }

}
