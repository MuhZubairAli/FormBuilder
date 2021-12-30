package pk.gov.pbs.formbuilder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import pk.gov.pbs.database.ModelBasedDatabaseHelper;
import pk.gov.pbs.database.exceptions.UnsupportedDataType;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Annexure;
import pk.gov.pbs.formbuilder.models.BackupHistory;
import pk.gov.pbs.formbuilder.models.SectionContext;
import pk.gov.pbs.formbuilder.models.LoginPayload;
import pk.gov.pbs.formbuilder.models.Option;

public class FormBuilderDatabase extends ModelBasedDatabaseHelper {
    private static final String dbName = "Main.db";
    private static final int dbVersion = Constants.DATABASE_VERSION;
    private static FormBuilderDatabase INSTANCE;
    private final Class<?>[] SURVEY_MODELS = new Class[]{
        Annexure.class, Option.class, BackupHistory.class, LoginPayload.class, SectionContext.class
    };

    private FormBuilderDatabase(Context context) {
        super(context, dbName, dbVersion);
    }

    public static FormBuilderDatabase getInstance(Context context){
        if (INSTANCE == null){
            synchronized (FormBuilderDatabase.class){
                INSTANCE = new FormBuilderDatabase(context);
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            for (Class<?> model : SURVEY_MODELS){
                createTable(model, db);
            }
        } catch (UnsupportedDataType unsupportedDataType) {
            unsupportedDataType.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<?> model : SURVEY_MODELS){
            dropTable(model, db);
        }
        onCreate(db);
    }

}
