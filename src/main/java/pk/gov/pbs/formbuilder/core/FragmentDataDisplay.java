package pk.gov.pbs.formbuilder.core;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import pk.gov.pbs.database.IDatabaseOperation;
import pk.gov.pbs.database.ModelBasedDatabaseHelper;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.utils.StaticUtils;

public class FragmentDataDisplay extends Fragment {
    public static final Gson gson;
    private final ViewModelFormSection mViewModel;
    private final int mSectionNumber;
    private ViewGroup mContainerDataDisplay;
    private final OnRequestDisplayData mDisplayRequest;

    static {
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    public FragmentDataDisplay(int sectionNumber, ViewModelFormSection viewModel) {
        super();
        mViewModel = viewModel;
        mSectionNumber = sectionNumber;

        Class<?> model = mViewModel.getMetaManifest().getModel(mSectionNumber);
        mDisplayRequest = new OnRequestDisplayData() {
            @Override
            public List<?> onRequestFetchData(ModelBasedDatabaseHelper db) {
                return db.selectRowsBySQL(
                        model
                        ,"SELECT * FROM `" + model.getSimpleName() + "` WHERE pcode=? AND hhno=?"
                        , new String[]{
                                mViewModel.getFormContext().getPCode(),
                                mViewModel.getFormContext().getHHNo().toString()
                        }
                );
            }
        };
    }

    public FragmentDataDisplay(int sectionNumber, ViewModelFormSection viewModel, OnRequestDisplayData requestDisplayData) {
        super();
        mSectionNumber = sectionNumber;
        mViewModel = viewModel;
        mDisplayRequest = requestDisplayData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_data_display, container, false);
        mContainerDataDisplay = fragmentView.findViewById(R.id.container_data_display);
        return fragmentView;
    }

    public void refreshData(){
        mViewModel
            .getFormRepository()
            .executeDatabaseOperation(new IDatabaseOperation<List<?>>() {
                @Override
                public List<?> execute(ModelBasedDatabaseHelper db) {
                    return mDisplayRequest.onRequestFetchData(db);
                }

                @Override
                public void postExecute(List<?> result) {
                    if (mContainerDataDisplay == null){
                        StaticUtils.getHandler().postDelayed(()->{
                            if (mContainerDataDisplay != null)
                                mDisplayRequest.onRequestDrawTable(result, mContainerDataDisplay);
                            else if (getView() != null)
                                mDisplayRequest.onRequestDrawTable(result, (ViewGroup) getView());
                        },1000);
                        return;
                    }
                    mDisplayRequest.onRequestDrawTable(result, mContainerDataDisplay);
                }
            });
    }

    public abstract static class OnRequestDisplayData{
        protected abstract List<?> onRequestFetchData(ModelBasedDatabaseHelper db);
        protected void onRequestDrawTable(List<?> data, ViewGroup viewContainer){
            TextView textView = viewContainer.findViewById(R.id.tv_display);
            if (textView != null) {
                textView.setText(gson.toJson(data));
            }
        }
    }
}