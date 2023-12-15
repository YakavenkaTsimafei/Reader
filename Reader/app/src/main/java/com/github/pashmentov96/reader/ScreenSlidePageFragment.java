package com.github.pashmentov96.reader;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScreenSlidePageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_PAGE_TEXT = "arg_page_text";
    int pageNumber;
    String text;
    static TextView translationOfWord;

    public static ScreenSlidePageFragment newInstance(int page, String textOfPage) {
        ScreenSlidePageFragment screenSlidePageFragment = new ScreenSlidePageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putString(ARGUMENT_PAGE_TEXT, textOfPage);
        screenSlidePageFragment.setArguments(arguments);
        return screenSlidePageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        text = getArguments().getString(ARGUMENT_PAGE_TEXT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

        ClickableTextView page = rootView.findViewById(R.id.textOfPage);

        translationOfWord.setText("");

        page.setText(text);

        Log.d("Pages", pageNumber + ": " + text);

        page.setTextWithAllWords(translationOfWord);

        return rootView;
    }
}
