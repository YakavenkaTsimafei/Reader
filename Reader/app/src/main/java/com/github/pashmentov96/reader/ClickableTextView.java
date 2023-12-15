package com.github.pashmentov96.reader;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ClickableTextView extends androidx.appcompat.widget.AppCompatTextView {
    public ClickableTextView(Context context) {
        super(context);
    }

    public ClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    final int color = getResources().getColor(R.color.black);

    public void setTextWithAllWords(TextView translationOfWord) {
        setMovementMethod(LinkMovementMethod.getInstance());
        setText(addClickablePart(getText().toString(), translationOfWord), BufferType.SPANNABLE);
    }

    private SpannableStringBuilder addClickablePart(String str, TextView translationOfWord) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = -1;

        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '\n' || str.charAt(i) == ' ') {
                int idx2 = i;
                if (idx1 != -1) {
                    ClickableWord clickableWord = new ClickableWord(str.substring(idx1, idx2), color, translationOfWord);
                    ssb.setSpan(clickableWord.getClickableSpan(), idx1, idx2, 0);
                }
                idx1 = -1;
            } else {
                if (idx1 == -1) {
                    idx1 = i;
                }
            }
        }

        if (idx1 != -1) {
            int idx2 = str.length();
            ClickableWord clickableWord = new ClickableWord(str.substring(idx1, idx2), color, translationOfWord);
            ssb.setSpan(clickableWord.getClickableSpan(), idx1, idx2, 0);
        }

        return ssb;
    }

    @Override
    public void scrollTo(int x, int y) {
        //super.scrollTo(x, y);
    }

    public static class ClickableWord {
        private String word;
        private ClickableSpan clickableSpan;

        public ClickableWord(final String word, final int color, final TextView translationOfWord) {
            this.word = word;
            this.clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    ScreenSlidePagerActivity.clickedWord = word;
                    Log.d("MyLogs", "Click on " + word);
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected void onPostExecute(String s) {
                            Log.d("MyLogs", "json1 = " + s);
                            translationOfWord.setText(parseFromJson(s));
                            Log.d("MyLogs", "json2 = " + s);
                        }

                        @Override
                        protected String doInBackground(Void... voids) {
                            return translate(word);
                        }
                    }.execute();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setColor(color);
                }
            };
        }


private String parseFromJson(String json) {
    try {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getJSONObject("data")
                .getJSONArray("translations")
                .getJSONObject(0)
                .getString("translatedText");
    } catch (JSONException e) {
        return "Error1: " + e.getMessage();
    }
}

private String translate(String word) {
    String apiKey = "adfa376021msh7427a12a06b92fcp15d25ajsnb5e62b5468d3";
    try {
        String urlParameters = "q=" + URLEncoder.encode(word, "UTF-8") + "&target=ru&source=en";
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        URL url = new URL("https://google-translate1.p.rapidapi.com/language/translate/v2");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept-Encoding", "application/gzip");
        conn.setRequestProperty("X-RapidAPI-Key", apiKey);
        conn.setRequestProperty("X-RapidAPI-Host", "google-translate1.p.rapidapi.com");

        try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }

        InputStream response = conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(response))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        Log.d("MyLogs", "Response: " + sb.toString());
        return sb.toString();
    } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
    }
}


        /**
         * @return the word
         */
        public String getWord() {
            return word;
        }

        /**
         * @return the clickableSpan
         */
        public ClickableSpan getClickableSpan() {
            return clickableSpan;
        }
    }
}
