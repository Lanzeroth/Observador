package com.ocr.observador.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.ocr.observador.MainActivity;
import com.ocr.observador.R;
import com.ocr.observador.events.StartCameraIntentEvent;
import com.ocr.observador.model.ModelCheckList;

import java.util.List;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ObservationsFragment extends Fragment {

    public static boolean isFirstTime = true;


    public ObservationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_observations, container, false);

        LinearLayout categoriesContainer = (LinearLayout) view.findViewById(R.id.categoriesContainer);


        ButterKnife.inject(view);


//        if (isFirstTime) {
            constructCategoryButtons(categoriesContainer);
            isFirstTime = false;
//        }



//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ((LinearLayout)categoriesContainer).removeAllViewsInLayout();
//            }
//        }, 1500);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.bus.post(getResources().getString(R.string.titlesData));

    }

    /**
     * This will be called by the main activiy once we have the classifications from the backend
     *
     * @param categoriesContainer
     */
    public void constructCategoryButtons(LinearLayout categoriesContainer) {
        List<ModelCheckList> list = queryCheckList();
        for (ModelCheckList item : list) {
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams linearLayoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            LinearLayout linearLayoutButton = new LinearLayout(getActivity());
            linearLayoutButton.setOrientation(LinearLayout.VERTICAL);
            linearLayoutButton.setLayoutParams(linearLayoutParams2);


            Button button = new Button(getActivity());
            button.setBackground(getResources().getDrawable(R.drawable.custom_button_cancel));
            button.setText(item.name);

            LinearLayout linearLayoutChecklist = new LinearLayout(getActivity());
            linearLayoutChecklist.setOrientation(LinearLayout.VERTICAL);
            linearLayoutButton.setLayoutParams(linearLayoutParams);

            CheckBox checkList = new CheckBox(getActivity());
            checkList.setText("Observacion 1");
            CheckBox checkList2 = new CheckBox(getActivity());
            checkList2.setText("Observacion 2");
            CheckBox checkList3 = new CheckBox(getActivity());
            checkList3.setText("Observacion 3");

            linearLayoutChecklist.addView(checkList);
            linearLayoutChecklist.addView(checkList2);
            linearLayoutChecklist.addView(checkList3);

            linearLayoutButton.addView(button);
            linearLayoutButton.addView(linearLayoutChecklist);

            categoriesContainer.addView(linearLayoutButton);

            //categoriesEvent.getCategoryProperties;
            LinearLayout linearLayoutMediaButtons = new LinearLayout(getActivity());
            linearLayoutMediaButtons.setOrientation(LinearLayout.HORIZONTAL);

            linearLayoutMediaButtons.setLayoutParams(linearLayoutParams2);

            ImageButton buttonPicture = new ImageButton(getActivity());
            buttonPicture.setId(new Integer(23));
            buttonPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePicture(1);
                }
            });
            buttonPicture.setBackground(getResources().getDrawable(R.drawable.custom_button_ocr));
            buttonPicture.setImageDrawable(getResources().getDrawable(R.mipmap.ic_camera_alt_white_36dp));

            ImageButton buttonVideo = new ImageButton(getActivity());
            buttonVideo.setId(new Integer(24));
            buttonVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeVideo(1);
                }
            });
            buttonVideo.setBackground(getResources().getDrawable(R.drawable.custom_button_ocr));
            buttonVideo.setImageDrawable(getResources().getDrawable(R.mipmap.ic_videocam_white_36dp));

            linearLayoutMediaButtons.addView(buttonPicture);
            linearLayoutMediaButtons.addView(buttonVideo);
            categoriesContainer.addView(linearLayoutMediaButtons);
        }

    }


    private void visibilityToggle(View v) {
        if (v.getVisibility() == View.GONE) {
            v.setVisibility(View.VISIBLE);
        } else if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        }
    }

    public List<ModelCheckList> queryCheckList() {
        if (MainActivity.nationalIdSelected != null) {
            return new Select().
                    from(ModelCheckList.class).
                    where("nationalId = ?", MainActivity.nationalIdSelected).
                    execute();
        }
        return null;
    }

    /**
     * Will try to take a VIDEO with the built in camera, upload it to GCS and then come back to
     * the response below with the image name to be put into the json
     *
     * @param category the "category" / checklist / division that was clicked
     */
    private void takeVideo(int category) {
        MainActivity.bus.post(new StartCameraIntentEvent(StartCameraIntentEvent.Type.STARTED, false, 1, category));
    }

    /**
     * Will try to take an IMAGE with the built in camera, upload it to GCS and then come back to
     * the response below with the image name to be put into the json
     *
     * @param category the "category" / checklist / division that was clicked
     */
    private void takePicture(int category) {
        MainActivity.bus.post(new StartCameraIntentEvent(StartCameraIntentEvent.Type.STARTED, true, 1, category));
    }


}
