package com.ocr.observador.jobs;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.ocr.observador.MainActivity;
import com.ocr.observador.events.GetMarkersEvent;
import com.ocr.observador.model.ModelMarker;
import com.ocr.observador.utilities.Priority;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

import static com.ocr.observador.events.GetMarkersEvent.Type;

/**
 * GetMarkersJob
 */
public class GetMarkersJob extends Job {
    boolean responseOk = false;
    boolean retry = true;

    public GetMarkersJob() {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-markers"));
    }

    @Override
    public void onAdded() {
        Logger.d("GetMarkersJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        eraseMarkersDataFromLocalDB();
        markerMaker();
        MainActivity.bus.post(new GetMarkersEvent(Type.COMPLETED, 1));
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

    /**
     * Test only
     * TODO: remove
     */
    public void markerMaker() {
        ActiveAndroid.beginTransaction();
        try {
            ModelMarker modelMarker = new ModelMarker(1, "Casilla 1", "", 28.63087284, -106.07098103);
            modelMarker.save();
            ModelMarker modelMarker2 = new ModelMarker(2, "Casilla 2", "", 28.6330293, -106.06827736);
            modelMarker2.save();
            ModelMarker modelMarker3 = new ModelMarker(3, "Casilla 3", "", 28.62896118, -106.06752634);
            modelMarker3.save();
            ModelMarker modelMarker4 = new ModelMarker(4, "Casilla 4", "", 28.62954504, -106.07362032);
            modelMarker4.save();
            ModelMarker modelMarker5 = new ModelMarker(5, "Casilla 5", "", 28.63470548, -106.07664585);
            modelMarker5.save();
            ModelMarker modelMarker6 = new ModelMarker(6, "Casilla 6", "", 28.63990332, -106.06883526);
            modelMarker6.save();

            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("GetMarkersJob canceled");
        MainActivity.bus.post(new GetMarkersEvent(Type.COMPLETED, 99));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
