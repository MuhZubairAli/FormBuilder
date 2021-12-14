package pk.gov.pbs.formbuilder.core;

public interface IMetaManifest {
    String[] section_status = new String[]{"Form Not Opened","Form Opened", "Form Closed"};
    String[] block_status = new String[] { "Not Started", "Incomplete", "Completed", "Synced", "Partially Synced" };
    String[] form_status = new String[]{
            "Pending"
            , "Completed"
            , "Partially Refused"
            , "Refusal"
            , "Non-Contacted"
    };

    String[] region = new String[]{ "", "Rural", "Urban", "Urban"};
    String[] backupType = new String[]{"Manual", "Automatic"};
    String[] gender = new String[]{
            "",
            "Male",
            "Female",
            "Transgender"
    };

    Class<?> getSection(int sectionNumber);
    int getSectionNumberFromClass(Class<?> section);
    Class<?>[] getModels();
    Class<?> getModel(int modelNumber);
    Class<?> getSection(String sectionIdentifier);
    Class<?> getModel(String modelIdentifier);
    String getSectionIdentifier(int sectionNumber);
    String[] getSectionIdentifiers();
    boolean isValidIndex(int index);
    int getVersion();
    String getRelationshipLabel(int relationCode, int genderCode);
    Class<?> getRosterSection();
    Class<?> getStarterActivity();
}
