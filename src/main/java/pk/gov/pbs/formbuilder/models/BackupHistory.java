package pk.gov.pbs.formbuilder.models;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public class BackupHistory extends Table {
    @NotNull
    @Unique
    public String ebcode;

    public int type;

    public BackupHistory(){}

    public BackupHistory(String ebCode, int type){
        super();
        this.ebcode = ebCode;
        this.type = type;
    }
}
