package pk.gov.pbs.formbuilder.core;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pk.gov.pbs.utils.exceptions.InvalidIndexException;
import pk.gov.pbs.utils.ExceptionReporter;

public abstract class ErrorStatementProvider {
    private static final Map<String, String> did = new HashMap<>(); //Datum Identifier Descriptor
    private static final Map<String, String> statements = new HashMap<>();
    private static final List<String> dynamicStatements = new ArrayList<>();
    private static int errorIndex = 0;

    static {
        for (Field field : ErrorStatementProvider.class.getDeclaredFields()){
            if (field.getType() == String.class){
                try {
                    field.set(null, generateErrorCode());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected ErrorStatementProvider() {
        try {
            initialize();
        } catch (Exception e) {
            ExceptionReporter.printStackTrace(e);
        }
    }

    public static class Make {
        public static String integrityViolationBetween(String index1, String index2){
            final String integrity_violation_bw = "Please check questions regarding {1} and {2}, answers of these questions are not appropriate.";
            dynamicStatements.add(integrity_violation_bw.replace("{1}", "{"+index1+"}").replace("{2}", "{"+index2+"}"));
            return "gen_"+ (dynamicStatements.size()-1);
        }
    }

    protected static String generateErrorCode(){
        return ++errorIndex + "";
    }

    protected void addErrorStatement(String key, String statement) throws InvalidIndexException {
        if (statements.containsKey(key))
            throw new InvalidIndexException();

        statements.put(key, statement);
    }

    protected void addDatumDescriptor(String datum, String description) throws InvalidIndexException {
        if (did.containsKey(datum))
            throw new InvalidIndexException();

        did.put(datum, description);
    }

    public String getStatement(String errorCode){
        try {
            String label;
            if(errorCode.length() > 4 && errorCode.substring(0,4).equalsIgnoreCase("gen_")){
                int index = Integer.parseInt(errorCode.substring(errorCode.indexOf('_')+1));
                label = dynamicStatements.get(index);
                dynamicStatements.remove(index);
            } else
                label = statements.get(errorCode);

            Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(label);
            while(m.find()) {
                System.out.println(m.group(1));
                label = label.replace("{" + m.group(1) + "}", Objects.requireNonNull(did.get(m.group(1))));
            }
            return label;
        } catch (Exception e){
            e.printStackTrace();
            return "invalid error code: " + errorCode;
        }
    }

    protected abstract void initialize() throws Exception;
}
