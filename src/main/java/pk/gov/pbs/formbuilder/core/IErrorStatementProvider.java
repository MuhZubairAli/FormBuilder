package pk.gov.pbs.formbuilder.core;

public interface IErrorStatementProvider {
    String getStatement(String key);
    String getErrorCode(String error);
}
