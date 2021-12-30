package pk.gov.pbs.formbuilder.database.dao;

import java.util.List;
import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.database.ModelBasedRepository;
import pk.gov.pbs.formbuilder.models.SectionContext;
import pk.gov.pbs.formbuilder.models.RosterSection;

public class HouseholdMemberDao<T extends RosterSection>{
    ModelBasedRepository repository;
    Class<T> mMemberClass;
    public HouseholdMemberDao(ModelBasedRepository repository, Class<T> memberModelClass){
        this.repository = repository;
        mMemberClass = memberModelClass;
    }

    public long insert(T member) {
        return DatabaseUtils.getFutureValue(
                repository.insert(member)
        );
    }

    public int update(T member) {
        return DatabaseUtils.getFutureValue(
                repository.update(member)
        );
    }

    public Future<List<T>> getAll(SectionContext fc) {
        return repository.selectRowMultiAs(
                mMemberClass,
                "SELECT * FROM " + mMemberClass.getSimpleName() + " WHERE pcode=? AND hhno=?",
                new String[]{fc.getBlockIdentifier(), String.valueOf(fc.getHHNo())}
        );
    }
}
