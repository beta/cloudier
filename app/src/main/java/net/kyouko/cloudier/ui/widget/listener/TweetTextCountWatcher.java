package net.kyouko.cloudier.ui.widget.listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import net.kyouko.cloudier.R;

/**
 * Implementation of {@link TextWatcher} to count tweet text.
 *
 * @author beta
 */
public class TweetTextCountWatcher implements TextWatcher {

    private TextView wordCountText;

    private int normalTextColor;
    private int errorTextColor;


    public TweetTextCountWatcher(TextView wordCountText) {
        this.wordCountText = wordCountText;

        Context context = wordCountText.getContext();
        normalTextColor = context.getResources().getColor(R.color.black_54alpha);
        errorTextColor = context.getResources().getColor(R.color.red_500);
    }


    public void applyWordCountAvailable(CharSequence charSequence) {
        int wordCount = 0;
        for (int i = 0; i < charSequence.length(); i += 1) {
            char c = charSequence.charAt(i);
            if (c < 128) {
                wordCount += 1;
            } else {
                wordCount += 2;
            }
        }
        wordCount /= 2;

        int wordCountAvailable = 140 - wordCount;
        wordCountText.setText(String.valueOf(wordCountAvailable));

        if (wordCountAvailable < 0) {
            wordCountText.setTextColor(errorTextColor);
        } else {
            wordCountText.setTextColor(normalTextColor);
        }
    }


    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        applyWordCountAvailable(charSequence);
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }


    @Override
    public void afterTextChanged(Editable editable) {
    }

}
