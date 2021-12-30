package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

/**
 * This class should be named as FormSectionContext but for convenience it is shortened
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
    public String bId;       //Block identifier
    @Nullable
    @Unique
    public Integer HHNo;       //Household number
    public Integer SNo;        //Serial number, Member id
    public Integer SeNo;       //Section number
    public Integer INo;        //Iteration number

    public SectionContext(){}

    public SectionContext(@NonNull String bId, @Nullable Integer HHNo, Integer seNo, Integer SNo, Integer INo) {
        this.bId = bId;
        this.HHNo = HHNo;
        this.SNo = SNo;
        this.SeNo = seNo;
        this.INo = INo;
    }

    public SectionContext(@NonNull String bId, @Nullable Integer HHNo, Integer seNo, Integer sNo) {
        this.bId = bId;
        this.HHNo = HHNo;
        this.SNo = sNo;
        this.SeNo = seNo;
    }

    public SectionContext(@NonNull String bId, @Nullable Integer HHNo, Integer SeNo) {
        this.bId = bId;
        this.HHNo = HHNo;
        this.SeNo = SeNo;
    }

    public SectionContext(@NonNull String bId, @Nullable Integer HHNo) {
        this.bId = bId;
        this.HHNo = HHNo;
    }

    public String getBlockIdentifier() {
        return bId;
    }

    @Nullable
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
        return new SectionContext(bId, HHNo, SeNo, SNo, INo);
    }

    public SectionContext cloneWithSection(int newSeNo) {
        return new SectionContext(bId, HHNo, newSeNo, SNo, INo);
    }

    public SectionContext cloneWithIteration(int newINo) {
        return new SectionContext(bId, HHNo, SeNo, SNo, newINo);
    }

    public SectionContext cloneWithMember(int newSNo) {
        return new SectionContext(bId, HHNo, SeNo, newSNo, INo);
    }

    public SectionContext cloneWith(int newSeNo, int newSNo) {
        return new SectionContext(bId, HHNo, newSeNo, newSNo, INo);
    }

    public SectionContext cloneWith(int newSeNo, int newSNo, int newINo) {
        return new SectionContext(bId, HHNo, newSeNo, newSNo, newINo);
    }
}
