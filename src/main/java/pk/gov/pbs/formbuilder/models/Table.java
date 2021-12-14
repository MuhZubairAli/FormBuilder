package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.SqlExclude;
import pk.gov.pbs.database.annotations.SqlPrimaryKey;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.database.annotations.Default;
import pk.gov.pbs.database.annotations.PrimaryKey;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.utils.ExceptionReporter;
import pk.gov.pbs.utils.SystemUtils;

public abstract class Table implements Serializable {
    @PrimaryKey
    @Expose
    @SerializedName("APID")
    public Long aid;

    @Nullable
    @Expose
    @SqlPrimaryKey()
    @SerializedName("ID")
    public Long sid;

    @NotNull
    @Expose
    @SerializedName("APTSCreated")
    public Long ts_created;

    @Nullable
    @Expose
    @SerializedName("APTSUpdated")
    public Long ts_updated;

    @Nullable
    @Expose
    @SqlExclude
    @SerializedName("IC")
    public String integrityCheck;

    @Default(value = "1")
    @Expose
    @SqlExclude
    @SerializedName("Status")
    public Integer status;

    @Expose
    @SqlExclude
    @SerializedName("TableName")
    public String tableName;

    public Table(){
        this.ts_created = SystemUtils.getUnixTs();
        tableName = this.getClass().getSimpleName();
    }

    //null safe equals
    protected boolean nse(Object value1, Object value2){
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;
        return String.valueOf(value1)
                .contentEquals(String.valueOf(value2));
    }

    public void set(String prop, @Nullable ValueStore value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getField(prop);

		if (value == null)
            field.set(this, null);
        else if (field.getType() == String.class)
            field.set(this, value.toString());
        else if (field.getType() == int.class || field.getType() == Integer.class)
            field.set(this, value.toInt());
        else if (field.getType() == long.class || field.getType() == Long.class)
            field.set(this, value.toLong());
        else if (field.getType() == double.class || field.getType() == Double.class)
            field.set(this, value.toDouble());
        else if (field.getType() == boolean.class || field.getType() == Boolean.class)
            field.set(this, value.toBoolean());
        else if (field.getType() == float.class || field.getType() == Float.class)
            field.set(this, value.toFloat());
        else if (field.getType() == char.class || field.getType() == Character.class)
            field.set(this, value.toChar());
    }

    public synchronized void setSynchronized(String prop, @Nullable ValueStore value) throws NoSuchFieldException, IllegalAccessException {
        set(prop, value);
    }

    public boolean set(String prop, @Nullable Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getField(prop);
        field.set(this, value);
        return true;
    }

    public synchronized boolean setSynchronized(String prop, @Nullable Object value) throws NoSuchFieldException, IllegalAccessException {
        return set(prop, value);
    }

    public ValueStore get(String prop) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getField(prop);
        if(field.get(this) != null)
            return new ValueStore(field.get(this).toString());
        return null;
    }

    public synchronized ValueStore getSynchronized(String prop) throws NoSuchFieldException, IllegalAccessException {
        return get(prop);
    }

    public boolean hasField(String field){
        try {
            Field f = getClass().getField(field);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public boolean checkDataIntegrity(){
        if (Constants.DEBUG_MODE)
            return true;

        if(integrityCheck != null){
            String has = integrityCheck;
            integrityCheck = null;
            String is = SystemUtils.MD5(toString());
            integrityCheck = has;
            return has.equalsIgnoreCase(is);
        }

        return false;
    }

    public void setupDataIntegrity(){
        integrityCheck = null;
        integrityCheck = SystemUtils.MD5(toString());
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            Field[] allFields = getClass().getFields();
            for (Field field : allFields){
                if(field.get(this) != null)
                    sb.append(field.get(this).toString());
            }
        } catch (Exception e){
            ExceptionReporter.printStackTrace(e);
        }
        return sb.toString();
    }

    public boolean isSame(Table subject,String... except){
        if (this.getClass().equals(subject.getClass())) {
            try {
                Field[] allFields = getClass().getFields();
                HashSet<Field> exceptFields = new HashSet<>();

                String[] ignoreFieldsInComparison = new String[] {"ts_updated", "integrityCheck", "status"};
                for (String field : ignoreFieldsInComparison) {
                    try {
                        Field oField = getClass().getField(field);
                        exceptFields.add(oField);
                    } catch (Exception ignore) { }
                }

                if (except != null && except.length > 0){
                    for (String field : except) {
                        if (field != null) {
                            try {
                                Field oField = getClass().getField(field);
                                exceptFields.add(oField);
                            } catch (Exception e) {
                                ExceptionReporter.printStackTrace(e);
                            }
                        }
                    }
                }

                for (Field field : allFields) {
                    if (!nse(field.get(this), field.get(subject))) {
                        if (!exceptFields.contains(field))
                            return false;
                    }
                }
            } catch (Exception e) {
                ExceptionReporter.printStackTrace(e);
                return false;
            }

            return true;
        }
        return false;
    }
}
