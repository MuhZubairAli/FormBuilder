package pk.gov.pbs.formbuilder.core;

import android.text.InputType;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.AskableAdapter;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputDate;
import pk.gov.pbs.formbuilder.inputs.singular.DateInput;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.DataInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.DateInputAdapter;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputAutoCompleteKeyboard;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputButtonedKBI;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputCBI;
import pk.gov.pbs.formbuilder.inputs.grouped.checked.GroupInputChecked;
import pk.gov.pbs.formbuilder.inputs.grouped.checked.GroupInputCheckedPredicate;
import pk.gov.pbs.formbuilder.inputs.grouped.checked.GroupInputRadioChecked;
import pk.gov.pbs.formbuilder.inputs.grouped.checked.subgroups.SGExpenditureKBI4x;
import pk.gov.pbs.formbuilder.inputs.grouped.checked.subgroups.SGExpenditureKBI8x;
import pk.gov.pbs.formbuilder.inputs.grouped.grid.GroupInputGridKBI;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputKBI2x;
import pk.gov.pbs.formbuilder.inputs.grouped.grid.GroupInputGrid;
import pk.gov.pbs.formbuilder.inputs.singular.AnnexInput;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.singular.AutoCompleteKeyboardInput;
import pk.gov.pbs.formbuilder.inputs.singular.CheckInput;
import pk.gov.pbs.formbuilder.inputs.singular.HouseholdMembersSpinnerInput;
import pk.gov.pbs.formbuilder.inputs.singular.KeyboardInput;
import pk.gov.pbs.formbuilder.inputs.singular.RadioInput;
import pk.gov.pbs.formbuilder.inputs.singular.Selectable;
import pk.gov.pbs.formbuilder.inputs.singular.SpecifiableCheckInput;
import pk.gov.pbs.formbuilder.inputs.singular.SpecifiableRadioInput;
import pk.gov.pbs.formbuilder.inputs.singular.SpinnerInput;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.AnnexInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.CheckInputAdapter;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.GroupInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.KeyboardInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.RadioInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.SpinnerInputAdapter;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputAnnex;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputCheckedKBI;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputCheckedKBI_SPI;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputKBI;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputKBI3x;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputRBI;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerMember;
import pk.gov.pbs.formbuilder.validator.Validator;

public class QuestionnaireBuilder {
    LabelProvider mLabelProvider;
    ArrayList<Askable> askables;
    Section model;
    public QuestionnaireBuilder(LabelProvider labelProvider) {
        mLabelProvider = labelProvider;
        askables = new ArrayList<>();
        model = null;
    }

    public QuestionnaireBuilder(LabelProvider labelProvider, HouseholdSection model) {
        mLabelProvider = labelProvider;
        askables = new ArrayList<>();
        this.model = model;
    }

    public void setModel(Section model) {
        this.model = model;
    }

    public Section getModel() {
        return model;
    }

    public String newLabel(String label){
        return mLabelProvider.newLabel(label);
    }

    public String newLabel(String label, String... args) {
        return mLabelProvider.newLabel(label, args);
    }

    public String getLabel(String index){
        return mLabelProvider.getLabel(index);
    }
    //===============================================================================
    //--------------------------- Household Members spinner -------------------------
    //===============================================================================

    public Question makeHouseholdMembers_SI(String qIndex, List<ItemSpinnerMember> extraOptions, @Nullable Validator validator){
        askables.add(
                new HouseholdMembersSpinnerInput(qIndex, extraOptions)
        );

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new SpinnerInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new SpinnerInputAdapter(abArr)
            );
        }

        if(model != null){
            try{
                ValueStore answer = model.get(qIndex);
                if(answer != null && !answer.isEmpty()){
                    question.loadAnswer(qIndex, answer);
                }
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}
        }

        askables.clear();

        return question;
    }

    public Question makeHouseholdMembers_SI(String qIndex, List<ItemSpinnerMember> extras){
        return makeHouseholdMembers_SI(qIndex, extras, null);
    }

    public Question makeHouseholdMembers_SI(String qIndex){
        return makeHouseholdMembers_SI(qIndex, null, null);
    }

    //================================================================================

    public Question makeSI(String qIndex, int oStart, int oEnd, @Nullable Validator validator){
        List<String> options = new ArrayList<>();
        String label;
        if (mLabelProvider.isLang("ur"))
            label = "مناسب آپشن کا انتخاب کریں";
        else
            label = "Select Appropriate Option";
        int count = oStart-1;
        String index;
        while (label != null || count <= oEnd) {
            if(label != null)
                options.add(label);
            index = qIndex + "_" + (++count);
            label = mLabelProvider.getLabel(index);
        }
        askables.add(
            new SpinnerInput(
                qIndex
                , options
            )
        );

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                qIndex
                , new SpinnerInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                qIndex
                , new SpinnerInputAdapter(abArr)
            );
        }

        if(model != null){
            question.loadModel(model);
//            if(abArr.length == 1){
//                try{
//                    ValueStore answer = model.get(qIndex);
//                    if(answer != null && !answer.isEmpty()){
//                        question.loadAnswer(qIndex, answer);
//                    }
//                } catch (IllegalAccessException ignored) {}
//                catch (NoSuchFieldException ignored) {}
//            } else {
//                for (Askable ab : abArr) {
//                    try {
//                        ValueStore answer = model.get(ab.getIndex());
//                        if (answer != null && !answer.isEmpty()) {
//                            question.loadAnswer(ab.getIndex(), answer);
//                        }
//                    } catch (IllegalAccessException | NoSuchFieldException ignored) {
//                    }
//                }
//            }
        }

        askables.clear();

        return question;
    }

    public Question makeSI(String qIndex, Validator validator){
        return makeSI(qIndex, 1, Constants.INVALID_NUMBER, validator);
    }

    public Question makeSI(String qIndex){
        return makeSI(qIndex, 1, Constants.INVALID_NUMBER, null);
    }

    public Question makeRBI(String qIndex, int oStart, int oEnd, @Nullable Validator validator, @Nullable HashMap<Integer, DatumIdentifier> specifiable){
        int count = oStart;
        String index = qIndex + "_" + count;

        while (mLabelProvider.hasLabel(index) || (count) <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                if (specifiable != null) {
                    if (specifiable.containsKey(count)) {
                        askables.add(new SpecifiableRadioInput(
                                index
                                , specifiable.get(count).setValue(new ValueStore(count))
                        ));
                    } else
                        askables.add(new RadioInput(index, new ValueStore(count)));
                } else
                    askables.add(new RadioInput(index, new ValueStore(count)));
            }

            index = qIndex + "_" + (++count);
        }

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new RadioInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new RadioInputAdapter(abArr)
            );
        }

        askables.clear();

        if(model != null){
            ValueStore answer = null;
            ValueStore sAnswer = null;
            try{
                answer = model.get(qIndex);
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

            try{
                sAnswer = model.get("__"+qIndex);
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

            if (answer != null && sAnswer != null)
                question.loadAnswer(qIndex + "_" + answer.toString(), answer, sAnswer);
            else if (answer != null)
                question.loadAnswer(qIndex + "_" + answer.toString(), answer);
        }

        return question;
    }

    public Question makeRBI(String qIndex, int oStart, Validator validator, HashMap<Integer, DatumIdentifier> specifiables){
        return makeRBI(qIndex, oStart, Constants.INVALID_NUMBER, validator, specifiables);
    }

    public Question makeRBI(String qIndex, int oStart, Validator validator){
        return makeRBI(qIndex, oStart, Constants.INVALID_NUMBER, validator, null);
    }

    public Question makeRBI(String qIndex, int oStart){
        return makeRBI(qIndex, oStart, Constants.INVALID_NUMBER, null, null);
    }

    public Question makeRBI(String qIndex, Validator validator, HashMap<Integer, DatumIdentifier> specifiable){
        return makeRBI(qIndex, 1, Constants.INVALID_NUMBER, validator, specifiable);
    }

    public Question makeRBI(String qIndex, Validator validator){
        return makeRBI(qIndex, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public Question makeRBI(String qIndex, HashMap<Integer, DatumIdentifier> specifiable){
        return makeRBI(qIndex, 1, Constants.INVALID_NUMBER, null, specifiable);
    }

    public Question makeRBI(String qIndex){
        return makeRBI(qIndex, 1, Constants.INVALID_NUMBER, null, null);
    }

    //=========================================================================================================


    public Question makeMultiColRBI(String qIndex, ColumnCount columnCount, int oStart, int oEnd, @Nullable Validator validator, @Nullable HashMap<Integer, DatumIdentifier> specifiable) {
        int count = oStart;
        String index = qIndex + "_" + count;

        while (mLabelProvider.hasLabel(index) || (count) <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                if (specifiable != null) {
                    if (specifiable.containsKey(count)) {
                        askables.add(new SpecifiableRadioInput(
                                index
                                ,  specifiable.get(count).setValue(new ValueStore(count))
                        ));
                    } else
                        askables.add(new RadioInput(index, new ValueStore(count)));
                } else
                    askables.add(new RadioInput(index, new ValueStore(count)));
            }

            index = qIndex + "_" + (++count);
        }

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new RadioInputAdapter(abArr, columnCount, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new RadioInputAdapter(abArr, columnCount, null)
            );
        }

        askables.clear();

        if(model != null){
            ValueStore answer = null;
            ValueStore sAnswer = null;
            try{
                answer = model.get(qIndex);
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

            try{
                sAnswer = model.get("__"+qIndex);
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

            if (answer != null && sAnswer != null)
                question.loadAnswer(qIndex + "_" + answer.toString(), answer, sAnswer);
            else if (answer != null)
                question.loadAnswer(qIndex + "_" + answer.toString(), answer);
        }

        return question;
    }

    public Question makeMultiColRBI(String qIndex, ColumnCount columnCount, Validator validator, HashMap<Integer, DatumIdentifier> specifiable){
        return makeMultiColRBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, specifiable);
    }

    public Question makeMultiColRBI(String qIndex, ColumnCount columnCount, HashMap<Integer, DatumIdentifier> specifiable){
        return makeMultiColRBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, null, specifiable);
    }

    public Question makeMultiColRBI(String qIndex, ColumnCount columnCount, Validator validator){
        return makeMultiColRBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public Question makeMultiColRBI(String qIndex, ColumnCount columnCount){
        return makeMultiColRBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, null, null);
    }

    //=========================================================================================================
    public Question makeMultiColCBI(String qIndex, ColumnCount columnCount, int oStart, int oEnd, @Nullable Validator validator, @Nullable HashMap<Integer, DatumIdentifier> specifiable){
        int count = oStart;
        String index = qIndex + count;

        while (mLabelProvider.hasLabel(index) || (count) <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                if (specifiable != null) {
                    if (specifiable.containsKey(count)) {
                        askables.add(new SpecifiableCheckInput(
                                index
                                , specifiable.get(count).setValue(count)
                        ));
                    } else
                        askables.add(new CheckInput(index, new ValueStore(count)));
                } else
                    askables.add(new CheckInput(index, new ValueStore(count)));
            }

            index = qIndex + (++count);
        }

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question = new Question(
                    qIndex
                    , new CheckInputAdapter(abArr, columnCount, validator)
            );
        } else {
            question = new Question(
                    qIndex
                    , new CheckInputAdapter(abArr, columnCount)
            );
        }

        if(model != null){
            for (Askable ab : abArr){
                ValueStore answer = null;
                ValueStore sAnswer = null;

                try {
                    answer = model.get(ab.getIndex());
                } catch (IllegalAccessException ignored) {}
                catch (NoSuchFieldException ignored) {}

                try {
                    sAnswer = model.get("__"+ab.getIndex());
                } catch (IllegalAccessException ignored) {}
                catch (NoSuchFieldException ignored) {}

                if(answer != null && !answer.isEmpty() && sAnswer != null && !sAnswer.isEmpty()) {
                    question.loadAnswer(qIndex + answer.toString(), answer, sAnswer);
                } else if(answer != null && !answer.isEmpty()){
                    question.loadAnswer(qIndex + answer.toString(), answer);
                }
            }
        }

        askables.clear();

        return question;
    }

    public Question makeMultiColCBI(String qIndex, ColumnCount columnCount, Validator validator, HashMap<Integer, DatumIdentifier> specifiable){
        return makeMultiColCBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, specifiable);
    }

    public Question makeMultiColCBI(String qIndex, ColumnCount columnCount, HashMap<Integer, DatumIdentifier> specifiable){
        return makeMultiColCBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, null, specifiable);
    }

    public Question makeMultiColCBI(String qIndex, ColumnCount columnCount, Validator validator){
        return makeMultiColCBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public Question makeMultiColCBI(String qIndex, ColumnCount columnCount){
        return makeMultiColCBI(qIndex, columnCount, 1, Constants.INVALID_NUMBER, null, null);
    }
    //=========================================================================================================
    public Question makeCBI(String qIndex, int oStart, int oEnd, @Nullable Validator validator, @Nullable HashMap<Integer, DatumIdentifier> specifiable){
        int count = oStart;
        String index = qIndex + count;

        while (mLabelProvider.hasLabel(index) || (count) <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                if (specifiable != null) {
                    if (specifiable.containsKey(count)) {
                        askables.add(new SpecifiableCheckInput(
                                index
                                , specifiable.get(count).setValue(count)
                        ));
                    } else
                        askables.add(new CheckInput(index, new ValueStore(count)));
                } else
                    askables.add(new CheckInput(index, new ValueStore(count)));
            }

            index = qIndex + (++count);
        }

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new CheckInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new CheckInputAdapter(abArr)
            );
        }

        if(model != null){
            for (Askable ab : abArr){
                ValueStore answer = null;
                ValueStore sAnswer = null;

                try{
                    answer = model.get(ab.getIndex());
                } catch (IllegalAccessException ignored) {}
                catch (NoSuchFieldException ignored) {}

                try{
                    sAnswer = model.get("__"+ab.getIndex());
                } catch (IllegalAccessException ignored) {}
                catch (NoSuchFieldException ignored) {}

                if(answer != null && !answer.isEmpty() && sAnswer != null && !sAnswer.isEmpty()) {
                    question.loadAnswer(qIndex + answer.toString(), answer, sAnswer);
                } else if(answer != null && !answer.isEmpty()){
                    question.loadAnswer(qIndex + answer.toString(), answer);
                }
            }
        }

        askables.clear();

        return question;
    }

    public Question makeCBI(String qIndex, Validator validator, HashMap<Integer, DatumIdentifier> specifiable){
        return makeCBI(qIndex, 1, Constants.INVALID_NUMBER, validator, specifiable);
    }

    public Question makeCBI(String qIndex, Validator validator){
        return makeCBI(qIndex, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public Question makeCBI(String qIndex, HashMap<Integer, DatumIdentifier> specifiable){
        return makeCBI(qIndex, 1, Constants.INVALID_NUMBER, null, specifiable);
    }

    public Question makeCBI(String qIndex){
        return makeCBI(qIndex, 1, Constants.INVALID_NUMBER, null, null);
    }

    public Question makeKBI(String qIndex, @Nullable Integer type, @Nullable Validator validator){
        if(type == null)
            type = InputType.TYPE_CLASS_TEXT;

        if(validator == null)
            askables.add(new KeyboardInput(qIndex, type));
        else
            askables.add(new KeyboardInput(qIndex, type, validator));

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new KeyboardInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new KeyboardInputAdapter(abArr)
            );
        }

        askables.clear();

        if(model != null){
            try{
                ValueStore answer = model.get(qIndex);
                if(answer != null && !answer.isEmpty()){
                    question.loadAnswer(qIndex, answer);
                }
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

        }

        return question;
    }

    public Question makeKBI(String qIndex, int type){
        return makeKBI(qIndex, type, null);
    }

    // Date Input
    public Question makeDI(String qIndex, @Nullable Validator validator){
        askables.add(new DateInput(qIndex, validator));
        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question = new Question(
                qIndex
                , new DateInputAdapter(abArr, validator)
        );

        askables.clear();

        if(model != null){
            question.loadModel(model);
        }

        return question;
    }

    public Question makeDI(String qIndex, DateInput[] dateInputs, @Nullable Validator validator){
        Question question = new Question(
                qIndex
                , new DateInputAdapter(dateInputs, validator)
        );

        askables.clear();

        if(model != null){
            question.loadModel(model);
        }

        return question;
    }


    public Question makeAI(String qIndex, DatumIdentifier identifier, @Nullable Validator validator){
        if(validator == null)
            askables.add(new AnnexInput(qIndex, identifier));
        else
            askables.add(new AnnexInput(qIndex, identifier, validator));

        SingularInput[] abArr = new SingularInput[askables.size()];
        askables.toArray(abArr);

        Question question;
        if(validator != null) {
            question =new Question(
                    qIndex
                    , new AnnexInputAdapter(abArr, validator)
            );
        }else {
            question =new Question(
                    qIndex
                    , new AnnexInputAdapter(abArr)
            );
        }

        askables.clear();

        if(model != null){
            try{
                ValueStore answer = model.get(qIndex);
                ValueStore desc = model.get(qIndex+"_desc");
                if(answer != null && !answer.isEmpty()){
                    question.loadAnswer(qIndex, answer, desc);
                }
            } catch (IllegalAccessException ignored) {}
            catch (NoSuchFieldException ignored) {}

        }

        return question;
    }

    public Question makeAI(String qIndex, DatumIdentifier identifier){
        return makeAI(qIndex, identifier, null);
    }


    //========================== DATA INPUT ===========================

    public Question makeDataInput(String qIndex, Askable[] askables){
        return new Question(
                qIndex,
                new DataInputAdapter(askables)
        );
    }

    public Question makeDataInput(String qIndex, List<Askable> askableList){
        Askable[] abbArr = new Askable[askableList.size()];
        abbArr = askableList.toArray(abbArr);
        return new Question(
                qIndex,
                new DataInputAdapter(abbArr)
        );
    }


    //========================================================================
    //----------------------GROUPED INPUT COMPONENTS--------------------------
    //========================================================================

    public Question makeGI(String qIndex, GroupInput[] askables, AskableAdapter.OnAnswerEvent onAnswerEvent){
        Question question;
        if(onAnswerEvent != null) {
            question = new Question(qIndex, new GroupInputAdapter(askables, onAnswerEvent));
        }else
            question = new Question(qIndex, new GroupInputAdapter(askables));

        if(model != null){
            question.loadModel(model);
        }

        return question;
    }

    public Question makeGI(String qIndex, GroupInput[] askables){
        return makeGI(qIndex, askables, null);
    }

    //============================================================================
    //---------------------- GROUP INPUT AKSABLE PREPARE -------------------------
    //============================================================================

    public GroupInputRBI prepareGI_RBI(String abIndex, ColumnCount columnCount, int oStart, int oEnd, Validator validator, HashMap<Integer, DatumIdentifier> specifiables){
        int count = oStart;
        String index = abIndex + "_" + count;

        while (mLabelProvider.hasLabel(index) || count <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                if (specifiables != null){
                    if (specifiables.containsKey(count)) {
                        askables.add(new SpecifiableRadioInput(
                                index
                                , specifiables.get(count).setValue(count)
                        ));
                    } else
                        askables.add(new RadioInput(index, new ValueStore(count)));
                }else
                    askables.add(new RadioInput(index, new ValueStore(count)));
            }

            index = abIndex + "_" + (++count);
        }

        Selectable[] abArr = new Selectable[askables.size()];
        askables.toArray(abArr);
        askables.clear();

        if (columnCount != null)
            return new GroupInputRBI(abIndex, abArr, columnCount, validator, getExtras(abIndex));
        else
            return new GroupInputRBI(abIndex, abArr, ColumnCount.DOUBLE, validator, getExtras(abIndex));
    }

    public GroupInputRBI prepareGI_RBI(String abIndex){
        return prepareGI_RBI(abIndex, null, 1, Constants.INVALID_NUMBER, null, null);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, ColumnCount columnCount){
        return prepareGI_RBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, null, null);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, Validator validator){
        return prepareGI_RBI(abIndex, null, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, Validator validator, HashMap<Integer, DatumIdentifier> specifiables){
        return prepareGI_RBI(abIndex, null, 1, Constants.INVALID_NUMBER, validator, specifiables);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, ColumnCount columnCount, Validator validator){
        return prepareGI_RBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, null);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, ColumnCount columnCount, Validator validator, HashMap<Integer, DatumIdentifier> specifiables){
        return prepareGI_RBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, validator, specifiables);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, ColumnCount columnCount, HashMap<Integer, DatumIdentifier> specifiables){
        return prepareGI_RBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, null, specifiables);
    }

    public GroupInputRBI prepareGI_RBI(String abIndex, HashMap<Integer, DatumIdentifier> specifiables){
        return prepareGI_RBI(abIndex, null, 1, Constants.INVALID_NUMBER, null, specifiables);
    }

    //============================================================================
    //---------------------- GROUP INPUT KEYBOARD GRID   -------------------------
    //============================================================================

    public GroupInputGrid prepareGI_GridKBI(String abIndex, ColumnCount columnCount, int inputType, int oStart, int oEnd, Validator validator){
        int count = oStart;
        String index = abIndex + count;

        while (mLabelProvider.hasLabel(index) || count <= oEnd) {
            askables.add(new KeyboardInput(index, inputType, validator));
            index = abIndex + (++count);
        }

        KeyboardInput[] abArr = new KeyboardInput[askables.size()];
        askables.toArray(abArr);
        askables.clear();

        return new GroupInputGridKBI(abIndex, columnCount, abArr, validator);
    }

    public GroupInputGrid prepareGI_GridKBI(String abIndex, ColumnCount columnCount, int inputType, char oStart, Validator validator){
        String index = abIndex + oStart;

        while (mLabelProvider.hasLabel(index)) {
            askables.add(new KeyboardInput(index, inputType, validator));
            index = abIndex + (++oStart);
        }

        KeyboardInput[] abArr = new KeyboardInput[askables.size()];
        askables.toArray(abArr);
        askables.clear();

        return new GroupInputGridKBI(abIndex, columnCount, abArr, validator);
    }

    public GroupInputGrid prepareGI_GridKBI(String abIndex, ColumnCount columnCount, int inputType, Validator validator, String... abIndices){
        for (String index : abIndices) {
            askables.add(new KeyboardInput(index, inputType, validator));
        }

        KeyboardInput[] abArr = new KeyboardInput[askables.size()];
        askables.toArray(abArr);
        askables.clear();

        return new GroupInputGridKBI(abIndex, columnCount, abArr, validator);
    }

    public GroupInputGrid prepareGI_GridKBI(String abIndex, ColumnCount columnCount, int inputType, String... abIndices){
        return prepareGI_GridKBI(abIndex, columnCount, inputType, null, abIndices);
    }

    //=========================================================================

    public GroupInputCBI prepareGI_CBI(String abIndex, ColumnCount columnCount, int oStart, int oEnd, Validator validator){
        int count = oStart;
        String index = abIndex + count;

        while (mLabelProvider.hasLabel(index) || count <= oEnd) {
            if(mLabelProvider.hasLabel(index)) {
                askables.add(new CheckInput(index, new ValueStore(count)));
            }

            index = abIndex + (++count);
        }

        CheckInput[] abArr = new CheckInput[askables.size()];
        askables.toArray(abArr);
        askables.clear();

        return new GroupInputCBI(abIndex, abArr, columnCount, validator);
    }

    public GroupInputCBI prepareGI_CBI(String abIndex){
        return prepareGI_CBI(abIndex, ColumnCount.DOUBLE, 1, Constants.INVALID_NUMBER, null);
    }

    public GroupInputCBI prepareGI_CBI(String abIndex, Validator validator){
        return prepareGI_CBI(abIndex, ColumnCount.DOUBLE, 1, Constants.INVALID_NUMBER, validator);
    }

    public GroupInputCBI prepareGI_CBI(String abIndex, ColumnCount columnCount){
        return prepareGI_CBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, null);
    }

    public GroupInputCBI prepareGI_CBI(String abIndex, ColumnCount columnCount, Validator validator){
        return prepareGI_CBI(abIndex, columnCount, 1, Constants.INVALID_NUMBER, validator);
    }

    //===============================================================================

    //------------ GROUP INPUT KBI -------------------------
    public GroupInputKBI prepareGI_KBI(String abIndex, int inputType, Validator validator){
        return new GroupInputKBI(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputKBI prepareGI_KBI(String abIndex, int inputType){
        return new GroupInputKBI(abIndex, inputType, getExtras(abIndex));
    }

    public GroupInputKBI prepareGI_KBI(String abIndex){
        return new GroupInputKBI(abIndex, InputType.TYPE_TEXT_FLAG_CAP_WORDS, getExtras(abIndex));
    }

    //--------------- GROUP INPUT DATE PICKER INPUT -------------
    public GroupInputDate prepareGI_DI(String abIndex, Validator validator){
        return new GroupInputDate(abIndex, validator);
    }

    public GroupInputDate prepareGI_DI(String abIndex){
        return new GroupInputDate(abIndex, null);
    }

    //------------- GROUP INPUT AUTO-COMPLETE KBI ---------------
    public GroupInputAutoCompleteKeyboard prepareGI_ACKBI(String abIndex, DatumIdentifier identifier, Validator validator){
        return new GroupInputAutoCompleteKeyboard(
                abIndex,
                new AutoCompleteKeyboardInput(abIndex, identifier, validator),
                validator
        );
    }

    public GroupInputAutoCompleteKeyboard prepareGI_ACKBI(String abIndex, DatumIdentifier identifier){
        return prepareGI_ACKBI(abIndex, identifier, null);
    }
    //----------------- GROUP INPUT KBI KBI KBI ------------------

    public GroupInputKBI3x prepareGI_KBI3x(String abIndex, int[] inputType, Validator... validator){
        return new GroupInputKBI3x(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputKBI3x prepareGI_KBI3x(String abIndex, int[] inputType, Validator validator){
        return new GroupInputKBI3x(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputKBI3x prepareGI_KBI3x(String abIndex, int inputType, Validator... validator){
        return new GroupInputKBI3x(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputKBI3x prepareGI_KBI3x(String abIndex, int inputType, Validator validator){
        return new GroupInputKBI3x(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputKBI3x prepareGI_KBI3x(String abIndex, int inputType){
        return new GroupInputKBI3x(abIndex, inputType, getExtras(abIndex));
    }

    //----------------- GROUP INPUT KBI KBI KBI ------------------

    public GroupInputKBI2x prepareGI_KBI2x(String abIndex, int[] inputType, Validator validator){
        return new GroupInputKBI2x(abIndex, inputType, validator);
    }

    public GroupInputKBI2x prepareGI_KBI2x(String abIndex, int[] inputType, Validator... validator){
        return new GroupInputKBI2x(abIndex, inputType, validator);
    }

    public GroupInputKBI2x prepareGI_KBI2x(String abIndex, int inputType, Validator... validator){
        return new GroupInputKBI2x(abIndex, new int[]{inputType}, validator);
    }

    public GroupInputKBI2x prepareGI_KBI2x(String abIndex, int inputType, String... extras){
        return new GroupInputKBI2x(abIndex, inputType, extras);
    }

    //----------------- GROUP INPUT BTN KBI ------------------

    public GroupInputButtonedKBI prepareGI_BtnKBI(String abIndex, int inputType, Validator validator, GroupInputButtonedKBI.OnButtonClick event){
        return new GroupInputButtonedKBI(abIndex, inputType, validator, event);
    }

    public GroupInputButtonedKBI prepareGI_BtnKBI(String abIndex, int inputType, GroupInputButtonedKBI.OnButtonClick event){
        return new GroupInputButtonedKBI(abIndex, inputType, event);
    }

    public GroupInputButtonedKBI prepareGI_BtnKBI(String abIndex, GroupInputButtonedKBI.OnButtonClick event){
        return new GroupInputButtonedKBI(abIndex, InputType.TYPE_CLASS_TEXT, event);
    }

    //------------------- GROUP INPUT ANNEX INPUT ----------------------

    public GroupInputAnnex prepareGI_AI(String abIndex, String identifier, Validator validator){
        return new GroupInputAnnex(abIndex, new DatumIdentifier(identifier), validator);
    }

    public GroupInputAnnex prepareGI_AI(String abIndex, String identifier){
        return new GroupInputAnnex(abIndex, new DatumIdentifier(identifier));
    }

    //----------------- GROUP INPUT CHECKED KBI -------------------------------
    public GroupInputCheckedKBI prepareGI_CKBI(String abIndex, int inputType, Validator validator){
        return new GroupInputCheckedKBI(abIndex, inputType, validator, getExtras(abIndex));
    }

    public GroupInputCheckedKBI prepareGI_CKBI(String abIndex, Validator validator){
            return new GroupInputCheckedKBI(abIndex, InputType.TYPE_CLASS_NUMBER, validator, getExtras(abIndex));
    }

    public GroupInputCheckedKBI prepareGI_CKBI(String abIndex){
        return new GroupInputCheckedKBI(abIndex, InputType.TYPE_CLASS_NUMBER, null, getExtras(abIndex));
    }

    //----------------- GROUP INPUT CHECKED KBI SPI ------------------

    public GroupInputCheckedKBI_SPI prepareGI_CKBI_SPI(String abIndex, int inputType){
        return new GroupInputCheckedKBI_SPI(abIndex, inputType);
    }
    public GroupInputCheckedKBI_SPI prepareGI_CKBI_SPI(String abIndex){
        return new GroupInputCheckedKBI_SPI(abIndex);
    }

    public GroupInputCheckedKBI_SPI prepareGI_CKBI_SPI(String abIndex, Validator validator){
        return new GroupInputCheckedKBI_SPI(abIndex, validator);
    }

    public GroupInputCheckedKBI_SPI prepareGI_CKBI_SPI(String abIndex, int inputType, Validator validator){
        return new GroupInputCheckedKBI_SPI(abIndex, inputType, validator);
    }

    public GroupInputCheckedKBI_SPI prepareGI_CKBI_SPI(String abIndex, int inputType, ArrayList<String> option, Validator validator){
        return new GroupInputCheckedKBI_SPI(abIndex, inputType, option, validator);
    }

    //---------------------- GROUP INPUT CHECKED ------------------------
    public GroupInputChecked prepareGI_CheckedInput(String abIndex, GroupInput[] subInputs, Validator validator){
        return new GroupInputChecked(
                abIndex,
                subInputs,
                validator,
                getExtras(abIndex)
        );
    }

    public GroupInputChecked prepareGI_CheckedInput(String abIndex, GroupInput subInput, Validator validator){
        return new GroupInputChecked(
                abIndex,
                subInput,
                validator,
                getExtras(abIndex)
        );
    }
    //---------------------- GROUP INPUT CHECKED (EXPENDITURES KBI 8X) ------------------------
    public GroupInputChecked prepareGI_CheckedExpenditureKBI8x(String abIndex, int inputType, Validator validator){
        return new GroupInputChecked(
                abIndex,
                new SGExpenditureKBI8x(abIndex, inputType, validator),
                validator,
                getExtras(abIndex)
        );
    }

    //---------------------- GROUP INPUT CHECKED (EXPENDITURES KBI 4X) ------------------------
    public GroupInputChecked prepareGI_CheckedExpenditureKBI4x(String abIndex, int inputType, Validator validator){
        return new GroupInputChecked(
                abIndex,
                new SGExpenditureKBI4x(abIndex, inputType, validator),
                validator,
                getExtras(abIndex)
        );
    }

    public GroupInputChecked prepareGI_ItemsOwnedKBI3x(String abIndex, int inputType, Validator validator){
        return new GroupInputChecked(
                abIndex,
                prepareGI_GridKBI (
                        abIndex,
                        ColumnCount.TRIPLE,
                        inputType,
                        validator,
                        abIndex+'a', abIndex+'b', abIndex+'c'
                ),
                null,
                getExtras(abIndex)
        );
    }
    //---------------------- GROUP INPUT RADIO CHECKED ----------------

    public GroupInputRadioChecked prepareGI_RadioCheckedInput(String abIndex, ColumnCount columnCount, GroupInputCheckedPredicate predicate, GroupInput[] subInput, Validator validator){
        return new GroupInputRadioChecked(
                abIndex,
                prepareGI_RBI(abIndex, columnCount),
                predicate,
                subInput,
                validator,
                getExtras(abIndex)
        );
    }

    public GroupInputRadioChecked prepareGI_RadioCheckedInput(String abIndex, ColumnCount columnCount, GroupInput[] subInput, Validator validator){
        return prepareGI_RadioCheckedInput(abIndex, columnCount, null, subInput, validator);
    }

    public GroupInputRadioChecked prepareGI_RadioCheckedInput(String abIndex, GroupInput[] subInput, Validator validator){
        return prepareGI_RadioCheckedInput(abIndex, ColumnCount.DOUBLE, subInput, validator);
    }

    public GroupInputRadioChecked prepareGI_RadioCheckedInput(String abIndex, GroupInputCheckedPredicate predicate, GroupInput subInput, Validator validator){
        return prepareGI_RadioCheckedInput(abIndex, ColumnCount.DOUBLE, predicate, new GroupInput[]{subInput}, validator);
    }

    public GroupInputRadioChecked prepareGI_RadioCheckedInput(String abIndex, GroupInput subInput, Validator validator){
        return prepareGI_RadioCheckedInput(abIndex, new GroupInput[]{subInput}, validator);
    }

    //----------------------------------------------------------------------
    public GroupInputChecked prepareGI_CheckedKBI3x(String abIndex, int inputType, Validator validator){
        return new GroupInputChecked(
                abIndex,
                new GroupInput[]{
                        prepareGI_GridKBI(
                                abIndex+"_",
                                ColumnCount.TRIPLE,
                                inputType,
                                validator,
                                abIndex+"a", abIndex+"b", abIndex+"c"
                        )
                },
                null,
                getExtras(abIndex)
        );
    }

    private String[] getExtras(String abIndex){
        if (mLabelProvider.hasLabel(abIndex + "_tv"))
            return new String[]{ mLabelProvider.getLabel(abIndex + "_tv") };
        else {
            if (mLabelProvider.hasLabel(abIndex + "_tv_1") && !mLabelProvider.hasLabel(abIndex + "_tv_2"))
                return new String[]{ mLabelProvider.getLabel(abIndex + "_tv_1") };
            else if (mLabelProvider.hasLabel(abIndex + "_tv_1") && mLabelProvider.hasLabel(abIndex + "_tv_2") && !mLabelProvider.hasLabel(abIndex + "_tv_3"))
                return new String[] {
                        mLabelProvider.getLabel(abIndex + "_tv_1"),
                        mLabelProvider.getLabel(abIndex + "_tv_2")
                };
            else {
                List<String> extrasList = new ArrayList<>();
                for (int i=1; true; i++) {
                    if (mLabelProvider.hasLabel(abIndex + "_tv_" + i))
                        extrasList.add(mLabelProvider.getLabel(abIndex + "_tv_" + i));
                    else break;
                }

                String[] extrasArray = new String[extrasList.size()];
                extrasArray = extrasList.toArray(extrasArray);
                return extrasArray;
            }
        }
    }
}
