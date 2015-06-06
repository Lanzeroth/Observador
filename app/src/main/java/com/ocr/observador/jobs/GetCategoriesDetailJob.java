package com.ocr.observador.jobs;


import com.ocr.observador.MainActivity;
import com.ocr.observador.events.RegisterUserEvent;
import com.ocr.observador.utilities.Priority;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


/**
 * GetCategoriesJob async job
 */
public class GetCategoriesDetailJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    private String[] safekeys;

    public GetCategoriesDetailJob(String[] safekeys) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-categories"));
        this.safekeys = safekeys;
    }

    @Override
    public void onAdded() {
        Logger.d("RegisterUserJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        for (String keys : safekeys) {
            //getstuff with this key
        }
        // Use a builder to help formulate the API request.
//        Backend.Builder builder = new Backend.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                new AndroidJsonFactory(),
//                null);
//        Backend service = builder.build();
//
//        MessagesCreateUser messagesCreateUser = new MessagesCreateUser();
//        messagesCreateUser.setAccountType(mAccountType);
//        messagesCreateUser.setAge(mAge);
//        messagesCreateUser.setEmail(mEmail);
//        messagesCreateUser.setName(mName);
//        messagesCreateUser.setInstallationId(mInstallationId);
//        ParseInstallation.getCurrentInstallation().getInstallationId();
//
//        MessagesCreateUserResponse response = service.user().create(messagesCreateUser).execute();
//
//        if (response.getOk()) {
//            Logger.json(response.toPrettyString());
//            MainActivity.bus.post(new RegisterUserEvent(RegisterUserEvent.Type.COMPLETED, 1));
//            responseOk = true;
//        } else {
//            if (response.getError().contains("User email already in platform")) {
//                Logger.d(response.getError());
//                MainActivity.bus.post(new RegisterUserEvent(RegisterUserEvent.Type.COMPLETED, 1));
//                retry = false;
//            } else {
//                Logger.e(response.getError());
//            }
//        }
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
