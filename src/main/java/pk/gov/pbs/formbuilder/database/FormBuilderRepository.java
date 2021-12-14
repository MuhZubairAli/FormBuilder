package pk.gov.pbs.formbuilder.database;

import android.app.Application;

import pk.gov.pbs.database.ModelBasedDatabaseHelper;
import pk.gov.pbs.database.ModelBasedRepository;
import pk.gov.pbs.formbuilder.database.dao.AnnexDao;
import pk.gov.pbs.formbuilder.database.dao.FormBuilderUtilsDao;
import pk.gov.pbs.formbuilder.database.dao.LoginDao;
import pk.gov.pbs.formbuilder.database.dao.OptionDao;

public class FormBuilderRepository extends ModelBasedRepository {
    protected final FormBuilderDatabase mFormBuilderDatabase;
    protected final OptionDao mOptionDao;
    protected final LoginDao mLoginDao;
    protected final AnnexDao mAnnexDao;
    protected final FormBuilderUtilsDao mFormUtils;

    public FormBuilderRepository(Application app){
        super(app);
        mFormBuilderDatabase = FormBuilderDatabase.getInstance(app);
        mOptionDao = new OptionDao(this);
        mLoginDao = new LoginDao(this);
        mAnnexDao = new AnnexDao(this);
        mFormUtils = new FormBuilderUtilsDao(this);
    }

    public OptionDao getOptionsDao() {
        return mOptionDao;
    }

    public LoginDao getLoginDao() {
        return mLoginDao;
    }

    public AnnexDao getAnnexDao() {
        return mAnnexDao;
    }

    public FormBuilderUtilsDao getUtilsDao(){
        return mFormUtils;
    }

    public ModelBasedDatabaseHelper getDatabase() {
        return mFormBuilderDatabase;
    }

}
