package com.ocr.observador.jobs;


import com.appspot.observador_electoral.backend.Backend;
import com.appspot.observador_electoral.backend.model.MessagesClasificacion;
import com.appspot.observador_electoral.backend.model.MessagesGetAvailableClasificaciones;
import com.appspot.observador_electoral.backend.model.MessagesGetAvailableClasificacionesResponse;
import com.appspot.observador_electoral.backend.model.MessagesGetClasificacionDetails;
import com.appspot.observador_electoral.backend.model.MessagesGetClasificacionDetailsResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.ocr.observador.MainActivity;
import com.ocr.observador.events.GetCategoriesEvent;
import com.ocr.observador.model.ModelCheckList;
import com.ocr.observador.utilities.Priority;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;


/**
 * GetCategoriesJob async job
 */
public class GetCategoriesJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    private String casilla_key;
    private String national_id;

    public GetCategoriesJob(String casilla, String nationalId) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-categories"));
        this.casilla_key = casilla;
        this.national_id = nationalId;
    }

    @Override
    public void onAdded() {
        Logger.d("RegisterUserJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();


        MessagesGetAvailableClasificaciones getter = new MessagesGetAvailableClasificaciones();
        getter.setCasilla(casilla_key);

        MessagesGetAvailableClasificacionesResponse response = service.clasificacion().getAvailable(getter).execute();

        if (response.getOk()) {
            List<String> clasificaciones = response.getClasificacion();
            for (String clasificacion : clasificaciones) {
                MessagesGetClasificacionDetails messagesGetClasificacionDetails = new MessagesGetClasificacionDetails();
                messagesGetClasificacionDetails.setClasificacion(clasificacion);

                MessagesGetClasificacionDetailsResponse detailsResponse =
                        service.clasificacion().getDetail(messagesGetClasificacionDetails).execute();

                if (detailsResponse.getOk()) {
                    MessagesClasificacion clasificacionToSave = detailsResponse.getClasificacion();

                    ModelCheckList modelCheckList = new ModelCheckList(
                            clasificacionToSave.getChecklist(),
                            clasificacionToSave.getName(),
                            clasificacionToSave.getRepeatable(),
                            national_id
                    );
                    modelCheckList.save();
                } else {
                    Logger.json(detailsResponse.toPrettyString());
                }

            }

        } else {
            Logger.json(response.toPrettyString());
        }

        MainActivity.bus.post(new GetCategoriesEvent(GetCategoriesEvent.Type.COMPLETED, 1, national_id));
    }

    @Override
    protected void onCancel() {
        Logger.d("RegisterUserJob canceled");
        MainActivity.bus.post(new GetCategoriesEvent(GetCategoriesEvent.Type.COMPLETED, 99, null));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
