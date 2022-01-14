package pk.gov.pbs.formbuilder.core;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.exceptions.InvalidQuestionStateException;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerSection;
import pk.gov.pbs.utils.ExceptionReporter;
import pk.gov.pbs.utils.UXEventListeners;

public abstract class ActivityHouseholdSection extends ActivityFormSection {
    protected Spinner mSpinnerSections;
    protected ArrayAdapter<ItemSpinnerSection> mAdapterSpinnerSections;

    @Override
    protected boolean shouldDownloadSectionEntries() {
        return true;
    }

    @Override
    protected void determineCourseOfAction() {
        if (mQuestionnaireManager == null || mQuestionnaireManager.getQuestions().size() > 0)
            return;

        if (mViewModel.getResumeModel() != null){
            resumeSection();
            return;
        }

        HouseholdSection model = mViewModel.getSectionEntryByFormContext(getSectionContext());
        if (model != null) {
            mViewModel.setResumeModel(model);
            preRepeatSection();
            mMap.setModel(model);
            mMap.reset();
            resumeSection();
        } else startSection();

    }

    protected void setupSpiSections(Spinner mSpinnerSections){
        ArrayList<ItemSpinnerSection> optSection = new ArrayList<>();
        optSection.add(new ItemSpinnerSection("Goto Section"));

        for (int section = getSectionNumber(); section > 1; section--){
            optSection.add(new ItemSpinnerSection("Section " + mMetaDataManifest.getSectionIdentifier(section),section));
        }

        mAdapterSpinnerSections = new ArrayAdapter<>(this, R.layout.item_list_sp, optSection);
        mAdapterSpinnerSections.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mSpinnerSections.setAdapter(mAdapterSpinnerSections);

        //Current sections selected by default
        mSpinnerSections.setSelection(1);

        mSpinnerSections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0) {
                    ItemSpinnerSection s = (ItemSpinnerSection) parent.getSelectedItem();
                    if(s.getSectionNumber() == getSectionNumber())
                        return;

                    if (s.getSectionNumber() != null) {
                        if (s.getSectionNumber() < getSectionNumber()) {
                            mUXToolkit.showConfirmDialogue("Are you sure to go back to '" + s.toString() + "'", new UXEventListeners.ConfirmDialogueEventsListener() {
                                @Override
                                public void onOK() {
                                    try {
                                        if (saveOrUpdateModel()) {
                                            Intent intent = new Intent(ActivityHouseholdSection.this, mMetaDataManifest.getSection(s.getSectionNumber()));
                                            intent.putExtra(Constants.Index.INTENT_EXTRA_SECTION_CONTEXT, mViewModel.getSectionContext().setSeNo(s.getSectionNumber()));
                                            startActivity(intent);
                                            ActivityHouseholdSection.this.finish();
                                        } else {
                                            ActivityHouseholdSection.this.mUXToolkit.showAlertDialogue("System failed to save form data. Try forcing exit and resuming the form.");
                                            mSpinnerSections.setSelection(1);
                                        }
                                    } catch (InvalidQuestionStateException e){
                                        mUXToolkit.showAlertDialogue(R.string.e110);
                                        mSpinnerSections.setSelection(1);
                                        ExceptionReporter.printStackTrace(e);
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    // back to current section
                                    mSpinnerSections.setSelection(1);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    protected void loadTopContainer(){
        if (mViewModel != null && mViewModel.getAssignment() != null) {
            ViewGroup containerTop = findViewById(R.id.container_toolbox);
            ViewGroup toolbox = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbox_form_household_section_filter_spi, containerTop);

            loadTopContainerSectionInfo(toolbox);

            mSpinnerSections = toolbox.findViewById(R.id.spi_1);
            setupSpiSections(mSpinnerSections);
        }
    }
}
