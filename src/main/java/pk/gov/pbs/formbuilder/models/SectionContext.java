package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

/**
 * This class could be named as FormSectionContext, but it is shortened for convenience
 * This class will be updated so that it's status will have following functional states
 * 1 -> Section not completed
 * 2 -> Section has completed
 *
 * When moving to next (or any other section) status of all eligible members would be checked
 * and if all member have completed than section's FormContext will be updated to 2 (section has completed)
 * otherwise default status all table is already 1 and here 1 = (section not completed)
 */
public class SectionContext extends Table implements Cloneable {
    @NotNull
    @Unique
    public String BId;       //Processing code
    @Nullable
    @Unique
    public Integer HHNo;       //Household number
    public Integer SNo;        //Serial number, Member id
    public Integer SeNo;       //Section number
    public Integer INo;        //Iteration number

    public SectionContext(){}

    public SectionContext(@NonNull String BId, @Nullable Integer HHNo, Integer seNo, Integer SNo, Integer INo) {
        this.BId = BId;
        this.HHNo = HHNo;
        this.SNo = SNo;
        this.SeNo = seNo;
        this.INo = INo;
    }

    public SectionContext(@NonNull String BId, @Nullable Integer HHNo, Integer seNo, Integer sNo) {
        this.BId = BId;
        this.HHNo = HHNo;
        this.SNo = sNo;
        this.SeNo = seNo;
    }

    public SectionContext(@NonNull String BId, @Nullable Integer HHNo, Integer SeNo) {
        this.BId = BId;
        this.HHNo = HHNo;
        this.SeNo = SeNo;
    }

    public SectionContext(@NonNull String BId, @Nullable Integer HHNo) {
        this.BId = BId;
        this.HHNo = HHNo;
    }

    public String getBlockIdentifier() {
        return BId;
    }

    public Integer getHHNo() {
        return HHNo;
    }

    public Integer getMemberID() {
        return SNo;
    }

    public Integer getSection() {
        return SeNo;
    }

    public Integer getIterationNumber() {
        return INo;
    }

    public SectionContext setSNo(Integer sNo){
        SNo = sNo;
        return this;
    }

    public SectionContext setSeNo(int seNo){
        SeNo = seNo;
        return this;
    }

    public SectionContext setINo(Integer iNo){
        INo = iNo;
        return this;
    }

    @Override
    public SectionContext clone() {
        return new SectionContext(BId, HHNo, SeNo, SNo, INo);
    }

    public SectionContext cloneWithSection(int newSeNo) {
        return new SectionContext(BId, HHNo, newSeNo, SNo, INo);
    }

    public SectionContext cloneWithIteration(int newINo) {
        return new SectionContext(BId, HHNo, SeNo, SNo, newINo);
    }

    public SectionContext cloneWithMember(int newSNo) {
        return new SectionContext(BId, HHNo, SeNo, newSNo, INo);
    }

    public SectionContext cloneWith(int newSeNo, int newSNo) {
        return new SectionContext(BId, HHNo, newSeNo, newSNo, INo);
    }

    public SectionContext cloneWith(int newSeNo, int newSNo, int newINo) {
        return new SectionContext(BId, HHNo, newSeNo, newSNo, newINo);
    }
}
