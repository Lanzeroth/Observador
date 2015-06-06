package com.ocr.observador.events;

/**
 * Created by Vazh on 25/5/2015.
 */
public class GetCategoriesEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    private String national_id;

    public GetCategoriesEvent(Enum type, int _resultCode, String national_id) {
        super(type);
        this._resultCode = _resultCode;
        this.national_id = national_id;
    }

    public String getNationalId() {
        return national_id;
    }

    public int getResultCode() {
        return _resultCode;
    }

}
