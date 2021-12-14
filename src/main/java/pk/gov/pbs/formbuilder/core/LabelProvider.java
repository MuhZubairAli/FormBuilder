package pk.gov.pbs.formbuilder.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import pk.gov.pbs.formbuilder.meta.Constants;

public abstract class LabelProvider {
    private static int counter = 0;
    protected String lang;
    protected HashMap<String,String> labels;
    protected HashMap<String,String> altLabels;
    protected HashMap<String,String> hints;
    protected HashMap<String,String> placeholders;
    protected HashMap<String,Integer> countableLabels;
    protected String[] nominals = new String[]{ "Zero", "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth" };
    public LabelProvider(){
        this("en");
    }

    public LabelProvider(String lang){
        this.lang = lang;
        labels = new HashMap<>();
        altLabels = new HashMap<>();
        hints = new HashMap<>();
        placeholders = new HashMap<>();
        loadDefaultLabels();
        loadLabels();
    }

    protected void loadDefaultLabels(){
        if(isLang("en")){
            addLabel(Constants.Index.QUESTION_SECTION_END,"Section Completed");
            addLabel(Constants.Index.LABEL_BTN_NEXT_SECTION, "Go to Next Section");
            addLabel(Constants.Index.LABEL_BTN_NEXT_ITERATION, "Add New Iteration");
            addLabel(Constants.Index.LABEL_BTN_REPEAT,"Repeat Section For Next Member");
            addLabel(Constants.Index.LABEL_BTN_NEXT_HH,"Go To Next Household");
            addLabel(Constants.Index.LABEL_BTN_UPDATE, "Update Section");
        }
    }

    void loadLabels(){
        if(isLang("en"))
            en();
//        else if(isLocale("ur"))
//            ur();
    }

    public String newLabel(String label) {
        String index = "new_"+counter++;
        labels.put(index,label);
        return index;
    }

    public String newLabel(String label, String... args) {
        String index = "new_"+counter++;
        if (args != null && args.length > 0){
            for (int i=1; i <= args.length; i++){
                label = label.replace("{:"+i+"}", args[i-1]);
            }
        }

        labels.put(index,label);
        return index;
    }

    protected void addLabel(String key, String value){
        labels.put(key,value);
    }

    protected void addAlternateLabel(String key, String value){
        altLabels.put(key,value);
    }

    protected void addHint(String key, String value){
        hints.put(key, value);
    }

    public void addPlaceholder(String key, String value){
        placeholders.put(key, value);
    }

    public String getLabel(String col){
        String label = labels.get(col);
        return output(col, label);
    }

    public String getHint(String col){
        String label = hints.get(col);
        return output(col, label);
    }

    public boolean hasLabel(String index){
        return labels.containsKey(index);
    }

    public boolean hasHint(String index){
        return hints.containsKey(index);
    }

    private String output(@NonNull String index, @Nullable String label){
        if(label != null) {
            int s = label.indexOf("{:");
            if (s != -1) {
                int e = label.indexOf('}');
                String key = label.substring(s + 2, e);

                if (placeholders.containsKey(key)) {
                    return label.replace("{:" + key + '}', "<b><i><u>" + placeholders.get(key) + "</u></i></b>");
                }

                if(altLabels.containsKey(index))
                    return altLabels.get(index);
            }

            return label;
        }
        return null;
    }

    public String getCountableLabel(String index){
        String label = labels.get(index);
        if(label != null) {
            if(countableLabels == null)
                countableLabels = new HashMap<>();

            if(countableLabels.containsKey(index))
                countableLabels.put(index, countableLabels.get(index)+1);
            else {
                if(index.equalsIgnoreCase(Constants.Index.LABEL_BTN_NEXT_ITERATION))
                    countableLabels.put(index, 2);
                else
                    countableLabels.put(index, 1);
            }

            return label.replace("{:count}", nominals[countableLabels.get(index)]);
        }
        return null;
    }

    public boolean isLang(String lang){
        return this.lang.equalsIgnoreCase(lang);
    }

    public void reset(){
        placeholders.clear();
    }

    public abstract void en();
    //abstract void ur();

}
