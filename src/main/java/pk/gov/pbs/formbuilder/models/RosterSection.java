package pk.gov.pbs.formbuilder.models;

public abstract class RosterSection extends MemberSection {
    public abstract String getName();
    public abstract Integer getRelationCode();
    public abstract Integer getGenderCode();
    public abstract Integer getAge();
    public abstract Integer getMaritalStatus();
}
