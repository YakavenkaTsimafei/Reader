package com.github.pashmentov96.reader;

import java.util.ArrayList;
import java.util.Arrays;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenSlidePagerActivity extends AppCompatActivity implements View.OnClickListener {


    private ViewPager viewPager;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LAST_PAGE = "last_page";
    private PagerAdapter pagerAdapter;

    String textOfBook;
    static final String ARGUMENT_TEXT = "argument_text";
    TextView translationOfWord;
    private String[] arrWords;
    int numPages;

    public static String clickedWord;

    public static Intent getIntent(Context context, String text) {
        Intent intent = new Intent(context, ScreenSlidePagerActivity.class);
        intent.putExtra(ARGUMENT_TEXT, text);
        return intent;
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.traslationOfWord) {
            if (clickedWord.length() > 0) {

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(v.getContext(), clickedWord + " " + getResources().getString(R.string.added_to_wordlist), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        addWord(clickedWord, String.valueOf(translationOfWord.getText()));
                        return null;
                    }
                }.execute();
            }
        }
    }

    private void addWord(String word, String translate) {
        Log.d("Mt log1", word);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> wordMap = new HashMap<>();
        wordMap.put(word, translate);

        db.collection("Words").document("Translate").update(wordMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MainActivity", "Word successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MainActivity", "Error writing word", e);
                    }
                });
        Log.d("Mt log2", word);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);
        Toolbar myToolBar = findViewById(R.id.my_toolbar_2);
        setSupportActionBar(myToolBar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        translationOfWord = findViewById(R.id.traslationOfWord);
        translationOfWord.setOnClickListener(this);
        ScreenSlidePageFragment.translationOfWord = translationOfWord;
        textOfBook = getIntent().getStringExtra(ARGUMENT_TEXT);
        arrWords = textOfBook.split(" ");
        numPages = (arrWords.length + 64) / 65;
        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int lastPage = settings.getInt(LAST_PAGE, 0);
        viewPager.setCurrentItem(lastPage);

    }



    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LAST_PAGE, viewPager.getCurrentItem());
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int beginIndex = 65 * position;
            int endIndex = Math.min(beginIndex + 65, arrWords.length);
            String pageText = String.join(" ", Arrays.copyOfRange(arrWords, beginIndex, endIndex));
            return ScreenSlidePageFragment.newInstance(position, pageText);
        }

        @Override
        public int getCount() {
            return numPages;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

}
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String TAG = "ScreenSlidePagerActivity";
//        String KEY_TITLE = "title";
//        String KEY_DESCRIPTION = "description";
//        db.collection("Notebook").document("My First Note").set(word)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(ScreenSlidePagerActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(ScreenSlidePagerActivity.this, "Error!", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, e.toString());
//                    }
//                });
//        Log.d("Mt log2",word);

//        SomePreferences somePreferences = new SomePreferences(this);
//        String token = somePreferences.getToken();
//        HttpURLConnection connection = null;
//        try {
//            URL url = new URL("http://d6719ff8.ngrok.io/api/add/" + word);
//
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//            connection.setRequestProperty  ("Authorization", "Bearer " + token);
//
//            Log.d("MyLogs", "Code " + connection.getResponseCode() + "; " + "Message " + connection.getResponseMessage());
//        } catch(Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
//        return null;
