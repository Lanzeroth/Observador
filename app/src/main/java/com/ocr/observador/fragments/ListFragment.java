package com.ocr.observador.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.query.Select;
import com.ocr.observador.MainActivity;
import com.ocr.observador.R;
import com.ocr.observador.custom.recyclerView.CustomAdapter;
import com.ocr.observador.model.ModelMarker;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<ModelMarker> mDataset;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataset();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(mDataset);

        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    private void initDataset() {
        mDataset = getMarkers();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.bus.post(getResources().getString(R.string.titlesMapFragment));

    }

    /**
     * DB query
     *
     * @return db Markers
     */
    public List<ModelMarker> getMarkers() {
        return new Select().from(ModelMarker.class).execute();
    }

}
