package com.ocr.observador.jobs;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.appspot.observador_electoral.backend.Backend;
import com.appspot.observador_electoral.backend.model.MessagesCasilla;
import com.appspot.observador_electoral.backend.model.MessagesGetCasillaDetail;
import com.appspot.observador_electoral.backend.model.MessagesGetCasillaDetailResponse;
import com.appspot.observador_electoral.backend.model.MessagesGetCasillasAssignedToObservador;
import com.appspot.observador_electoral.backend.model.MessagesGetCasillasAssignedToObservadorResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.ocr.observador.MainActivity;
import com.ocr.observador.events.GetCasillasSafeKeyEvent;
import com.ocr.observador.model.ModelMarker;
import com.ocr.observador.utilities.Priority;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;


/**
 * GetCasillasSafeKeyJob
 */
public class GetCasillasSafeKeyJob extends Job {
    boolean responseOk = false;
    boolean retry = true;

    String mEMail;

    public GetCasillasSafeKeyJob(String email) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-markers"));
        mEMail = email;
    }

    @Override
    public void onAdded() {
        Logger.d("GetCasillasSafeKeyJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();


        MessagesGetCasillasAssignedToObservador getter = new MessagesGetCasillasAssignedToObservador();
        getter.setEmail(mEMail);


        MessagesGetCasillasAssignedToObservadorResponse response = service.casilla().getAssignedToObservador(getter).execute();

        if (response.getOk()) {
            eraseMarkersDataFromLocalDB();
            Logger.json(response.toPrettyString());
            List<String> casillas = response.getCasillas();
            for (String casilla : casillas) {
                MessagesGetCasillaDetail messagesGetCasillaDetail = new MessagesGetCasillaDetail();
                messagesGetCasillaDetail.setCasilla(casilla);

                MessagesGetCasillaDetailResponse casillaResponse = service.casilla().get(messagesGetCasillaDetail).execute();

                if (casillaResponse.getOk()) {
                    MessagesCasilla casillaToSave = casillaResponse.getCasilla();

                    String[] parts = casillaToSave.getLoc().split(",");
                    String latitude = parts[0];
                    String longitude = parts[1];

                    ModelMarker modelMarker = new ModelMarker(
                            casillaToSave.getAddress(),
                            casillaToSave.getDistrito(),
                            casillaToSave.getNationalId(),
                            casillaToSave.getName(),
                            casilla, //this will be the urlsafekey
                            Double.parseDouble(latitude),
                            Double.parseDouble(longitude)
                    );
                    modelMarker.save();
                } else {
                    Logger.json(casillaResponse.toPrettyString());
                }
            }
            responseOk = true;

        } else {
            Logger.d(response.getError());

        }


        MainActivity.bus.post(new GetCasillasSafeKeyEvent(GetCasillasSafeKeyEvent.Type.COMPLETED, 1));
    }

    /**
     * Database erase
     * Erases the db so we don't have to check if the reading already exists and don't put duplicates * erases all the bills so we don't have to compare
     */
    private void eraseMarkersDataFromLocalDB() {
        List<ModelMarker> tempList = new Select().from(ModelMarker.class).execute();
        if (tempList != null && tempList.size() > 0) {
            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ModelMarker.class).execute();
                ActiveAndroid.setTransactionSuccessful();
            } catch (Exception e) {
                Logger.e(e, "error deleting existing db");
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

//    /**
//     * Test only
//     * TODO: remove
//     */
//    public void markerMaker() {
//        ActiveAndroid.beginTransaction();
//        try {
//            ModelMarker modelMarker = new ModelMarker("Partido Liberal", "distrito 8", "112312", 1, "Casilla 1", "", 28.63087284, -106.07098103);
//            modelMarker.save();
//            ModelMarker modelMarker2 = new ModelMarker("Partido Liberal", "distrito 8", "112312",2, "Casilla 2", "", 28.6330293, -106.06827736);
//            modelMarker2.save();
//            ModelMarker modelMarker3 = new ModelMarker("Partido Liberal", "distrito 8", "112312",3, "Casilla 3", "", 28.62896118, -106.06752634);
//            modelMarker3.save();
//            ModelMarker modelMarker4 = new ModelMarker("Partido Liberal", "distrito 8", "112312",4, "Casilla 4", "", 28.62954504, -106.07362032);
//            modelMarker4.save();
//            ModelMarker modelMarker5 = new ModelMarker("Partido Liberal", "distrito 8", "112312",5, "Casilla 5", "", 28.63470548, -106.07664585);
//            modelMarker5.save();
//            ModelMarker modelMarker6 = new ModelMarker("Partido Liberal", "distrito 8", "112312",6, "Casilla 6", "", 28.63990332, -106.06883526);
//            modelMarker6.save();
//
//            ActiveAndroid.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            ActiveAndroid.endTransaction();
//        }
//    }

    @Override
    protected void onCancel() {
        Logger.d("GetCasillasSafeKeyJob canceled");
        MainActivity.bus.post(new GetCasillasSafeKeyEvent(GetCasillasSafeKeyEvent.Type.COMPLETED, 99));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
