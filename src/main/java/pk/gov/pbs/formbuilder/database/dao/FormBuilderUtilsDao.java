package pk.gov.pbs.formbuilder.database.dao;

import java.util.List;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.database.FormBuilderRepository;
import pk.gov.pbs.formbuilder.models.BackupHistory;
import pk.gov.pbs.formbuilder.models.SectionContext;

public class FormBuilderUtilsDao {
    FormBuilderRepository mRepository;

    public FormBuilderUtilsDao(FormBuilderRepository mRepository) {
        this.mRepository = mRepository;
    }

    //@Query("SELECT * FROM fcs WHERE mPCode=:pcode and mHHNo=:hhno")
    public SectionContext getFCSByContext(SectionContext fContext){
        return mRepository.getDatabase().selectRowBySQL(
                SectionContext.class, "SELECT * FROM "+ SectionContext.class.getSimpleName()+" WHERE PCode=? and HHNo=?",
                new String[] {fContext.getBlockIdentifier(), String.valueOf(fContext.getHHNo())}
        );
    }

    //@Query("SELECT * FROM fcs WHERE mPCode=:pcode and mHHNo=:hhno")
    public SectionContext getFCS(String pcode, int hhno){
        return mRepository.getDatabase().selectRowBySQL(
                SectionContext.class, "SELECT * FROM "+ SectionContext.class.getSimpleName()+" WHERE PCode=? and HHNo=?",
                new String[] {pcode, String.valueOf(hhno)}
        );
    }

    public Long setSectionContext(SectionContext fContext){
        return DatabaseUtils.getFutureValue(
                mRepository.replaceOrThrow(fContext)
        );
    }

    //@Query("SELECT * FROM fcs")
    public List<SectionContext> getAllFCs(){
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                SectionContext.class, "SELECT * FROM "+ SectionContext.class.getSimpleName(), (String) null
        ));
    }

    //@Query("SELECT * FROM backup_history ORDER BY aid DESC LIMIT 30")
    public List<BackupHistory> getBackupHistory(){
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                BackupHistory.class,
                "SELECT * FROM "+BackupHistory.class.getSimpleName()+" ORDER BY aid DESC LIMIT 30",
                (String) null
        ));
    }

}
