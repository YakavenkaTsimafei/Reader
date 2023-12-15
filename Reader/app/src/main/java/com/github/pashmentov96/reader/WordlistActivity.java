package com.github.pashmentov96.reader;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class WordlistActivity extends AppCompatActivity {

    private TextView myDictionary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordlist);

        Toolbar childToolbar = findViewById(R.id.my_toolbar_2);
        setSupportActionBar(childToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        myDictionary = findViewById(R.id.myDictionary);
         myDictionary.setMovementMethod(LinkMovementMethod.getInstance());
        loadWordlist();
    }


    private Map<String, String> parseWordlistFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            HashMap<String, String> wordlist = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);
                Log.d("Wordlist1", key + ": " + value);
                wordlist.put(key, value);
            }
            return wordlist;
        } catch (JSONException e) {
            return null;
        }
    }

//    private void loadWordlist() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("Words").document("Translate")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                Log.d("MainActivity14", "DocumentSnapshot data: " + document.getData());
//                                String json = document.getData().toString();
//                                Map<String, String> dictionary = parseWordlistFromJson(json);
//                                StringBuilder stringBuilder = new StringBuilder();
//                                for (String key : dictionary.keySet()) {
//                                    stringBuilder.append(key + " - " + dictionary.get(key) + "\n");
//                                }
//                                if (stringBuilder.length() != 0) {
//                                    myDictionary.setText(stringBuilder.toString());
//                                }
//                            } else {
//                                Log.d("MainActivity", "No such document");
//                            }
//                        } else {
//                            Log.d("MainActivity", "get failed with ", task.getException());
//                        }
//                    }
//                });
//    }
//private void loadWordlist() {
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    db.collection("Words").document("Translate")
//            .get()
//            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            Log.d("MainActivity14", "DocumentSnapshot data: " + document.getData());
//                            String json = document.getData().toString();
//                            Map<String, String> dictionary = parseWordlistFromJson(json);
//                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//                            for (String key : dictionary.keySet()) {
//                                String word = key + " - " + dictionary.get(key) + "\n";
//                                SpannableString spannableString = new SpannableString(word);
//                                ClickableSpan clickableSpan = new ClickableSpan() {
//                                    @Override
//                                    public void onClick(@NonNull View widget) {
//                                        Log.d("ClickedWord", key);
//                                    }
//                                };
//                                spannableString.setSpan(clickableSpan, 0, key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                stringBuilder.append(spannableString);
//                            }
//                            if (stringBuilder.length() != 0) {
//                                myDictionary.setText(stringBuilder, TextView.BufferType.SPANNABLE);
//                            }
//                        } else {
//                            Log.d("MainActivity", "No such document");
//                        }
//                    } else {
//                        Log.d("MainActivity", "get failed with ", task.getException());
//                    }
//                }
//            });
//}
private void loadWordlist() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("Words").document("Translate")
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("MainActivity14", "DocumentSnapshot data: " + document.getData());
                            String json = document.getData().toString();
                            Map<String, String> dictionary = parseWordlistFromJson(json);
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                            for (String key : dictionary.keySet()) {
                                String word = key + " - " + dictionary.get(key) + "\n";
                                SpannableString spannableString = new SpannableString(word);
                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Log.d("ClickedWord", key);
                                        deleteWordFromFirebase(key);
                                    }
                                };
                                spannableString.setSpan(clickableSpan, 0, key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                stringBuilder.append(spannableString);
                            }
                            if (stringBuilder.length() != 0) {
                                myDictionary.setText(stringBuilder, TextView.BufferType.SPANNABLE);
                            }
                        } else {
                            Log.d("MainActivity", "No such document");
                        }
                    } else {
                        Log.d("MainActivity", "get failed with ", task.getException());
                    }
                }
            });
}

    private void deleteWordFromFirebase(String wordToDelete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Words").document("Translate")
                .update(wordToDelete, FieldValue.delete())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MainActivity", "Word successfully deleted!");
                        // Здесь можете обновить интерфейс или загрузить данные заново после удаления слова
                        loadWordlist();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MainActivity", "Error deleting word", e);
                    }
                });
    }
}
