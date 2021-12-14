package pk.gov.pbs.formbuilder.database.dao;

import java.util.List;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.database.FormBuilderRepository;
import pk.gov.pbs.formbuilder.models.Annexure;
import pk.gov.pbs.formbuilder.pojos.Annex;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;

public class AnnexDao {
    FormBuilderRepository mRepository;
    private static final String table = Annexure.class.getSimpleName();

    public AnnexDao(FormBuilderRepository mRepository) {
        this.mRepository = mRepository;
    }

    //@Query("SELECT `code`, `desc` FROM annexures WHERE identifier = :identifier")
    public List<Annex> getAnnexuresByIdentifier(String identifier){
        return DatabaseUtils.getFutureValue(mRepository.selectRowMultiAs(
                Annex.class,
                "SELECT `code`, `desc` FROM "+ table +" WHERE identifier=?",
                new String[]{identifier}
        ));
    }

    public List<Annex> getAnnexuresByIdentifier(DatumIdentifier identifier){
        return getAnnexuresByIdentifier(identifier.toString());
    }

    //@Query("SELECT `desc` FROM annexures WHERE `identifier`=:identifier AND `code`=:code")
    protected String doGetLabelFor(String identifier, String code) {
        return DatabaseUtils.getFutureValue(mRepository.selectColAs(
                String.class,
                "SELECT `desc` FROM "+Annexure.class.getSimpleName()+
                        " WHERE `identifier`=? AND `code`=?",
                new String[]{identifier, code}
        ));
    }

    public String getLabelFor(DatumIdentifier identifier, ValueStore value){
        return doGetLabelFor(identifier.toString(), value.toString());
    }

    //@Query("SELECT COUNT(*) FROM annexures WHERE identifier=:identifier")
    public Integer getAnnexCount(String identifier){
        return DatabaseUtils.getFutureValue(mRepository.selectColAs(
                Integer.class,
                "SELECT COUNT(*) FROM annexures WHERE identifier=:identifier",
                new String[]{ identifier }
        ));
    }
}
