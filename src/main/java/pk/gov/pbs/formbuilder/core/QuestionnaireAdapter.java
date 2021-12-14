package pk.gov.pbs.formbuilder.core;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.formbuilder.utils.ThemeUtils;

public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionViewHolder> {
    public static short FORCE_ASK_NEXT = 0;
    protected ActivityFormSection mContext;
    protected ArrayList<Question> mQuestions;
    //protected static Typeface UrduFontFace;

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion,tvQuestionHint;
        LinearLayout containerInput;

        QuestionViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            //tvQuestion.setTypeface(UrduFontFace);

            tvQuestionHint = itemView.findViewById(R.id.tv_question_hint);
            //tvQuestionHint.setTypeface(UrduFontFace);

            containerInput = itemView.findViewById(R.id.container_answer);

            FORCE_ASK_NEXT = 0;
            mQuestions.get(viewType).initialize(mContext, containerInput);

            //itemView.setOnClickListener((view)->{
            //    // in order to avoid focus on question on unintended click on question space
            //    if(view.hasFocus())
            //        view.clearFocus();
            //});

            itemView.findViewById(R.id.btn_ask_next).setOnClickListener(v -> {
                mContext.getNavigationToolkit().askNextQuestion(viewType);
            });

            itemView.setOnKeyListener((v, keyCode, event) -> {
                if(event.getAction() == KeyEvent.ACTION_UP && (keyCode == 62 || keyCode == 66))
                    mContext.getNavigationToolkit().askNextQuestion(viewType);
                return true;
            });

            if(mQuestions.get(viewType) instanceof QuestionActor || mQuestions.get(viewType).getState() == QuestionStates.READ_ONLY){
                itemView.findViewById(R.id.btn_lock).setVisibility(View.GONE);
                itemView.findViewById(R.id.btn_reset).setVisibility(View.GONE);
                itemView.findViewById(R.id.btn_ask_next).setVisibility(View.GONE);
            } else {
                /*if(mQuestions.get(viewType).getState() != QuestionStates.ANSWER_LOADED) {*/

                itemView.findViewById(R.id.btn_lock).setOnClickListener((View v) -> {
                    if (mQuestions.get(viewType).getState() == QuestionStates.LOCKED) {
                        mQuestions.get(viewType).unlock();
                    }else
                        if((viewType + 1) == mQuestions.size())
                            mContext.getNavigationToolkit().askNextQuestion();
                });

                itemView.findViewById(R.id.btn_reset).setOnLongClickListener(v -> {
                    if(mQuestions.get(viewType).isCritical()) {
                        mContext.restartFrom(mQuestions.get(viewType).getIndex());
                    }else
                        mQuestions.get(viewType).reset();
                    return true;
                });

                itemView.findViewById(R.id.btn_reset).setOnClickListener((View v)-> {
                    if(mQuestions.get(viewType).getState() == QuestionStates.UNLOCKED) {
                        boolean shouldFocus = true;
                        ValueStore[][] answers = mQuestions.get(viewType).getAnswers();
                        if(answers == null || (answers.length > 0 && answers[0] != null))
                            shouldFocus = true;
                        else {
                            for (ValueStore[] answer : answers){
                                if(answer[0] != null && !answer[0].isEmpty()){
                                    shouldFocus = false;
                                    break;
                                }
                            }
                        }

                        if(shouldFocus) {
                            mQuestions.get(viewType).requestFocus();
                            return;
                        }

                        mQuestions.get(viewType).reset();
                        mQuestions.get(viewType).requestInputFocus();
                        return;
                    }
                    mContext.getUXToolkit().showToast("Long press to reset the question");
                });
            }
        }
    }

    public QuestionnaireAdapter(ActivityFormSection context){
        this.mContext = context;
        this.mQuestions = context.getQuestionnaireManager().getQuestions();
        //UrduFontFace = Typeface.createFromAsset(context.getAssets(), "fonts/Jameel_Noori_Nastaleeq_Kasheeda.ttf");
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View questionCardView = mContext.getLayoutInflater().inflate(R.layout.item_layout_question, parent,false);
        return new QuestionViewHolder(questionCardView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = mQuestions.get(position);
        boolean isHeader = question instanceof QuestionHeader;
        Spanned qHTM = Html.fromHtml(mContext.getLabelProvider().getLabel(question.getIndex()));
        if(isHeader || question instanceof QuestionActor){
            holder.tvQuestion.setGravity(Gravity.CENTER_HORIZONTAL);
            if (isHeader) {
                holder.tvQuestionHint.setVisibility(View.GONE);
                holder.tvQuestion.setTextColor(Color.WHITE);
                holder.tvQuestion.setBackgroundColor(ThemeUtils.getColorByTheme(mContext, R.attr.colorAccentDark));
            }
        }
        holder.tvQuestion.setText(qHTM);
        if (mContext.getLabelProvider().hasHint(question.getIndex())) {
            String htm = mContext.getLabelProvider().getHint(question.getIndex());
            if (htm != null) {
                Spanned hHTM = Html.fromHtml(htm);
                holder.tvQuestionHint.setText(hHTM);
            }
        } else {
            holder.tvQuestionHint.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(mQuestions != null)
            return mQuestions.size();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
