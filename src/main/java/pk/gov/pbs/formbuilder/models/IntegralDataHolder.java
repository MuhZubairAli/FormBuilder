package pk.gov.pbs.formbuilder.models;

import java.lang.reflect.Field;
import java.util.List;

public abstract class IntegralDataHolder {
    public boolean setupDataIntegrity(){
        try {
            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                if (field.get(this) != null) {
                    for (Object section : ((List) field.get(this))){
                        ((Table) section).setupDataIntegrity();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean checkDataIntegrity(){
        try {
            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                if (field.get(this) != null) {
                    for (Object section : ((List) field.get(this))){
                        if (!((Table) section).checkDataIntegrity())
                            return false;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
