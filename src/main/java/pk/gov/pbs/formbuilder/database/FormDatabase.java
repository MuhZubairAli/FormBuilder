package pk.gov.pbs.formbuilder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;

import pk.gov.pbs.database.ModelBasedDatabaseHelper;
import pk.gov.pbs.database.exceptions.UnsupportedDataType;
import pk.gov.pbs.formbuilder.core.IMetaManifest;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.LoginPayload;

public class FormDatabase extends ModelBasedDatabaseHelper {
    private final IMetaManifest mModelManifest;
    private static FormDatabase INSTANCE;

    public FormDatabase(Context context, LoginPayload payload, IMetaManifest manifest) {
        super(context, payload.getUserName()+"_"+payload.gender+".db", Constants.DATABASE_VERSION + manifest.getVersion());
        mModelManifest = manifest;
    }

    public static FormDatabase getInstance(Context context, LoginPayload payload, IMetaManifest manifest){
        if (INSTANCE == null){
            synchronized (FormBuilderDatabase.class){
                INSTANCE = new FormDatabase(context, payload, manifest);
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HashSet<Class<?>> created = new HashSet<>();
        try {
            for (Class<?> model : mModelManifest.getModels()){
                if (!created.contains(model)) {
                    createTable(model, db);
                    created.add(model);
                }
            }
        } catch (UnsupportedDataType unsupportedDataType) {
            unsupportedDataType.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<?> model : mModelManifest.getModels()){
            dropTable(model, db);
        }
        onCreate(db);
    }

}
