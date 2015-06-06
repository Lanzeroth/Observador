package com.ocr.observador.jobs;


import com.appspot.observador_electoral.backend.Backend;
import com.appspot.observador_electoral.backend.model.MessagesCreateObservador;
import com.appspot.observador_electoral.backend.model.MessagesCreateObservadorResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.ocr.observador.MainActivity;
import com.ocr.observador.events.RegisterUserEvent;
import com.ocr.observador.utilities.Priority;
import com.orhanobut.logger.Logger;
import com.parse.ParseInstallation;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


/**
 * RegisterUserJob async job
 */
public class RegisterUserJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    private String mAccountType;
    private long mAge;
    private String mEmail;
    private String mName;
    private String mInstallationId;

    public RegisterUserJob(String mAccountType, long mAge, String mEmail, String mName, String mInstallationId) {
        super(new Params(Priority.MID).requireNetwork().groupBy("register-user"));
        this.mAccountType = mAccountType;
        this.mAge = mAge;
        this.mEmail = mEmail;
        this.mName = mName;
        this.mInstallationId = mInstallationId;
    }

    @Override
    public void onAdded() {
        Logger.d("RegisterUserJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
//         Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();

        MessagesCreateObservador messagesCreateObservador = new MessagesCreateObservador();
        messagesCreateObservador.setAccountType(mAccountType);
        messagesCreateObservador.setAge(mAge);
        messagesCreateObservador.setEmail(mEmail);
        messagesCreateObservador.setName(mName);
        messagesCreateObservador.setInstallationId(mInstallationId);
        ParseInstallation.getCurrentInstallation().getInstallationId();

        MessagesCreateObservadorResponse response = service.observador().create(messagesCreateObservador).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            MainActivity.bus.post(new RegisterUserEvent(RegisterUserEvent.Type.COMPLETED, 1));
            responseOk = true;
        } else {
            if (response.getError().contains("Observador email already in platform")) {
                Logger.d(response.getError());
                MainActivity.bus.post(new RegisterUserEvent(RegisterUserEvent.Type.COMPLETED, 1));
                retry = false;
            } else {
                Logger.e(response.getError());
            }
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("RegisterUserJob canceled");
        MainActivity.bus.post(new RegisterUserEvent(RegisterUserEvent.Type.COMPLETED, 99));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
