package pk.gov.pbs.formbuilder.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.core.IMetaManifest;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.database.FormBuilderRepository;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Option;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.pojos.OptionTuple;
import pk.gov.pbs.formbuilder.pojos.OptionTupleWeb;

/**
 * Options Data Access Class
 */
public class OptionDao {
    FormBuilderRepository mRepository;
    private static final String table = Option.class.getSimpleName();

    public OptionDao(FormBuilderRepository repository){
        mRepository = repository;
    }

    public Long insertVoidOption(){
        Option voidOption = new Option(0,0,0,"Void");
        voidOption.aid = 1000L;
        return DatabaseUtils.getFutureValue(
                mRepository.insert(voidOption)
        );
    }

    public Long insertZeroOption(DatumIdentifier identifier){
        Option option = new Option(identifier, mRepository.getContext().getString(R.string.s_s_l_2nd_default_value));
        Future<Long> future = mRepository.getExecutorService().submit(
                () -> {
                    long oid = hasUnspecifiedOption(identifier);
                    if(oid != Constants.INVALID_NUMBER)
                        return oid;
                    else
                        return mRepository
                                .getDatabase()
                                .insert(option);
                }
        );
        return DatabaseUtils.getFutureValue(future);
    }

    //@Query(""SELECT COUNT(*) FROM options")
    public Integer getCount(){
        return DatabaseUtils.getFutureValue(
                mRepository.selectColAs(Integer.class, "SELECT COUNT(*) FROM " + table, null)
        );
    }

    //@Query("SELECT sid FROM options WHERE sid is not null")
    public List<Long> getAllServerIDs(){
        return DatabaseUtils.getFutureValue(mRepository.selectColMultiAs(
                Long.class, "SELECT `sid` FROM " + table + " WHERE sid is not null",
                null
        ));
    }

    //@Query("SELECT max(sid) FROM options")
    public Long getMaxServerID(){
        return DatabaseUtils.getFutureValue(
                mRepository.selectColAs(Long.class, "SELECT max(sid) FROM " + table, null)
        );
    }

    //@Query("SELECT aid, sid, `desc` FROM options WHERE `s` =:s AND `c` = :c")
    private List<OptionTuple> doSearchOption(String s, String c){
        return DatabaseUtils.getFutureValue(
                mRepository.selectRowMultiAs(
                        OptionTuple.class, "SELECT `aid`, `sid`, `desc` FROM "+table+" WHERE `s`= ? AND `c`=?",
                        new String[]{s, c}
                )
        );
    }

    //@Query("SELECT aid, sid, `desc` FROM options WHERE `s`=:s AND `c` like :c AND `v`=:v")
    public List<OptionTuple> doSearchOption(int s, String c, int v) {
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                OptionTuple.class, "SELECT `aid`, `sid`, `desc` FROM "+table+" WHERE `s`= ? AND `c`=?  AND `v`=?",
                new String[]{String.valueOf(s), c, String.valueOf(v)}
        ));
    }

    //@Query("SELECT aid, sid, `desc` FROM options WHERE `s`=:s AND `c` like :c AND `v`=:v AND `desc` like :query")
    public List<OptionTuple> doSearchOption(String s, String c, String v, String query) {
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                OptionTuple.class, "SELECT `aid`, `sid`, `desc` FROM "+table+" WHERE `s`= ? AND `c`=?  AND `v`=? AND `desc` like ?",
                new String[]{s, c, v, query}
        ));
    }

    //@Query("SELECT aid, sid, `desc` FROM options WHERE `s`=:s AND `c` like :c AND `desc` like :query")
    public List<OptionTuple> doSearchOption(String s, String c, String v) {
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                OptionTuple.class, "SELECT `aid`, `sid`, `desc` FROM "+table+" WHERE `s`=? AND `c`=? AND `v`= ?",
                new String[]{s, c, v}
        ));
    }

    public List<OptionTuple> find(DatumIdentifier identifier, String query){
        if(query.length()<2)
            return null;

        StringBuilder sb = new StringBuilder();
        for (char c : query.toCharArray()){
            sb.append('%');
            sb.append(c);
        }
        sb.append('%');
        query = sb.toString();

        List<OptionTuple> result;
        if(identifier.value != null)
            result = doSearchOption(identifier.section,identifier.column,identifier.value.toString(),query);
        else
            result = doSearchOption(identifier.section,identifier.column,query);
        return result;
    }

    public List<OptionTuple> getOptionsByIdentifier(DatumIdentifier identifier){
        List<OptionTuple> result;
        if(identifier.value != null)
            result = doSearchOption(identifier.section,identifier.column,identifier.value.toString());
        else
            result = doSearchOption(identifier.section,identifier.column);
        return result;
    }

    //@Query("SELECT `desc` FROM options WHERE aid=:id OR sid=:id")
    public String getLabelFor(long id) {
        return DatabaseUtils.getFutureValue(mRepository.selectColAs(
                String.class,
                "SELECT `desc` FROM "+table+" WHERE aid=? OR sid=?",
                new String[]{String.valueOf(id), String.valueOf(id)}
        ));
    }

    ///@Query("SELECT * FROM options WHERE `sid` is null")
    public List<Option> getUnsyncedOptions() {
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                Option.class, "SELECT * FROM "+table+" WHERE `sid` is null", null
        ));
    }

    //@Query("UPDATE options SET sid=:server_id WHERE aid=:id")
    public Integer setSyncStatus(long server_id, long id) {
        Future<Integer> future = mRepository.getExecutorService().submit(
                () -> {
                    ContentValues cv = new ContentValues();
                    cv.put("sid", server_id);

                    return mRepository.getDatabase().getWritableDatabase().update(
                            table,
                            cv,
                            "aid=?",
                            new String[]{ String.valueOf(id) }
                    );
                }
        );
        return DatabaseUtils.getFutureValue(future);
    }

    //@Query("SELECT * FROM options ORDER BY aid")
    public List<Option> getAllOptions() {
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                Option.class,
                "SELECT * FROM "+table+" ORDER BY aid",
                null

        ));
    }

    /**
     * Helper methods
     */
    public long hasUnspecifiedOption(DatumIdentifier identifier){
        Option option;
        String opString = mRepository.getContext().getString(R.string.s_s_l_2nd_default_value);
        if(identifier.value != null)
            option = doUnspecifiedExists(identifier.section, identifier.column, identifier.value.toString(), opString);
        else
            option = doUnspecifiedExists(identifier.section, identifier.column, opString);

        if(option != null){
            if(option.sid != null)
                return option.sid;
            else
                return option.aid;
        }
        return Constants.INVALID_NUMBER;
    }

    /**
     * Private Helper Methods
     */
    //@Query("SELECT * FROM options WHERE `s`=:s AND `c` like :c AND `v`=:v AND `desc` LIKE :desc")
    private Option doUnspecifiedExists(String s, String c, String v, String desc){
        Future<Option> future = mRepository.getExecutorService().submit(()-> {
            Cursor cursor = mRepository.getDatabase().getReadableDatabase().query(
                    table,
                    DatabaseUtils.getAllColumns(Option.class),
                    "`s`=? AND `c` like ? AND `v`=? AND `desc` LIKE ?",
                    new String[] {String.valueOf(s), String.valueOf(c), String.valueOf(v), desc},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                Option result = mRepository.getDatabase().extractObjectFromCursor(Option.class, cursor);
                cursor.close();
                return result;
            }
            return null;
        });
        return DatabaseUtils.getFutureValue(future);
    }

    //@Query("SELECT * FROM options WHERE `s`=:s AND `c` like :c AND `desc` LIKE :desc")
    private Option doUnspecifiedExists(String s, String c, String desc){
        Future<Option> future = mRepository.getExecutorService().submit(()-> {
            Cursor cursor = mRepository.getDatabase().getReadableDatabase().query(
                    table,
                    DatabaseUtils.getAllColumns(Option.class),
                    "`s`=? AND `c` like ? AND `desc` LIKE ?",
                    new String[] {String.valueOf(s), String.valueOf(c), desc},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                Option result = mRepository.getDatabase().extractObjectFromCursor(Option.class, cursor);
                cursor.close();
                return result;
            }
            return null;
        });
        return DatabaseUtils.getFutureValue(future);
    }

    private int updateField(String table, String colName, String __colName, int colValue,long __colValue, long __colValueNew){
        ContentValues values = new ContentValues();
        values.put(__colName, __colValueNew);

        return mRepository.getDatabase().getWritableDatabase().updateWithOnConflict(
                table
                , values
                , colName + "=? AND "+__colName+"=?"
                , new String[]{ String.valueOf(colValue), String.valueOf(__colValue) }
                , SQLiteDatabase.CONFLICT_ABORT
        );
    }

    public int setupSyncedOptions(OptionTupleWeb option, IMetaManifest manifest) {
        if(option.s == 0)
            return Constants.INVALID_NUMBER;

        String tableName = String.format("s%s", manifest.getSectionIdentifier(option.s));
        String radioField = option.c;
        String __radioField = String.format("__%s", radioField);
        String checkField = String.format("%s%d",radioField,option.v);
        String __checkField = String.format("__%s", checkField);

        Field fieldExists;
        try {
            fieldExists = manifest.getModel(option.s).getField(__radioField);
            return updateField(tableName, radioField, __radioField, option.v, option.aid, option.sid);
        } catch (NoSuchFieldException e) {
            try {
                fieldExists = manifest.getModel(option.s).getField(__checkField);
                return updateField(tableName, checkField, __checkField, option.v, option.aid, option.sid);
            } catch (NoSuchFieldException nsf) {
                nsf.printStackTrace();
                return Constants.INVALID_NUMBER;
            }
        }
    }
}