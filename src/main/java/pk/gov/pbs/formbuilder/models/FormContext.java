package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

/**
 * This class will be updated so that it's status will have following functional states
 * 1 -> Section not completed
 * 2 -> Section has completed
 *
 * When moving to next (or any other section) status of all eligible members would be checked
 * and if all member have completed than section's FormContext will be updated to 2 (section has completed)
 * otherwise default status all table is already 1 and here 1 = (section not completed)
 */
public class FormContext extends Table implements Cloneable {
    @NotNull
    @Unique
    public String PCode;       //Processing code
    @Nullable
    @Unique
    public Integer HHNo;       //Household number
    public Integer SNo;        //Serial number, Member id
    public Integer SeNo;       //Section number
    public Integer INo;        //Iteration number

    public FormContext(){}

    public FormContext(@NonNull String PCode, @Nullable Integer HHNo, Integer seNo, Integer SNo, Integer INo) {
        this.PCode = PCode;
        this.HHNo = HHNo;
        this.SNo = SNo;
        this.SeNo = seNo;
        this.INo = INo;
    }

    public FormContext(@NonNull String PCode,@Nullable Integer HHNo, Integer seNo, Integer sNo) {
        this.PCode = PCode;
        this.HHNo = HHNo;
        this.SNo = sNo;
        this.SeNo = seNo;
    }

    public FormContext(@NonNull String PCode,@Nullable Integer HHNo, Integer SeNo) {
        this.PCode = PCode;
        this.HHNo = HHNo;
        this.SeNo = SeNo;
    }

    public FormContext(@NonNull String PCode,@Nullable Integer HHNo) {
        this.PCode = PCode;
        this.HHNo = HHNo;
    }

    public String getPCode() {
        return PCode;
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

    public FormContext setSNo(Integer sNo){
        SNo = sNo;
        return this;
    }

    public FormContext setSeNo(int seNo){
        SeNo = seNo;
        return this;
    }

    public FormContext setINo(Integer iNo){
        INo = iNo;
        return this;
    }

    @Override
    public FormContext clone() {
        return new FormContext(PCode, HHNo, SeNo, SNo, INo);
    }

    public FormContext cloneWithSection(int newSeNo) {
        return new FormContext(PCode, HHNo, newSeNo, SNo, INo);
    }

    public FormContext cloneWithIteration(int newINo) {
        return new FormContext(PCode, HHNo, SeNo, SNo, newINo);
    }

    public FormContext cloneWithMember(int newSNo) {
        return new FormContext(PCode, HHNo, SeNo, newSNo, INo);
    }

    public FormContext cloneWith(int newSeNo, int newSNo) {
        return new FormContext(PCode, HHNo, newSeNo, newSNo, INo);
    }

    public FormContext cloneWith(int newSeNo, int newSNo, int newINo) {
        return new FormContext(PCode, HHNo, newSeNo, newSNo, newINo);
    }
}
