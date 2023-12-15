package com.github.pashmentov96.reader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class MainActivity extends AppCompatActivity {

    Button buttonWordlist;
    Button buttonOpen;
    private GoogleSignInClient googleSignInClient;
    final int PICK_FILE_REQUEST = 10;
    final int REQUEST_READ_EXTERNAL_STORAGE = 5;
    final int OPEN_LOGIN_ACTIVITY = 20;
    StringBuilder stringBuilder = new StringBuilder();
    String textOfBook;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // SomePreferences somePreferences = new SomePreferences(this);
        MenuInflater inflater = getMenuInflater();
        //  if (somePreferences.getVariableIsLogged() == 0) {
        //     inflater.inflate(R.menu.menu_notlogged, menu);
        //  } else {
        inflater.inflate(R.menu.menu_logged, menu);
        // }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_login) {
            clickOnLoginButton();
            return true;
        } else if (id == R.id.action_logout) {
            clickOnLogoutButton();
            return true;
        } else if (id == R.id.action_about) {
            clickOnAboutButton();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void clickOnAboutButton() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void clickOnLoginButton() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, OPEN_LOGIN_ACTIVITY);
    }

    private void clickOnLogoutButton() {
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                    }
                });

    }

    private void clickOnOpenButton() {
        SomePreferences somePreferences = new SomePreferences(this);

        Intent intent = new Intent();
        intent.setType("application/epub+zip");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.choose_file)), PICK_FILE_REQUEST);

    }

    @SuppressLint("StaticFieldLeak")
    private void clickOnWordlistButton(View view) {
        SomePreferences somePreferences = new SomePreferences(this);
        if (somePreferences.getVariableIsLogged() == 0) {
            Toast.makeText(this, getResources().getString(R.string.must_be_logged_wordlist), Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, WordlistActivity.class));
        }
    }

    public void setLocale(String localeName) {
        SomePreferences somePreferences = new SomePreferences(this);
        String prevLocale = getResources().getConfiguration().locale.toString();
        if (prevLocale.contains("en")) {
            prevLocale = "en";
        }
        if (!localeName.equals(prevLocale)) {
            Locale myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(myLocale);
            res.updateConfiguration(conf, dm);
            somePreferences.setVariableLanguage(localeName);
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SomePreferences somePreferences = new SomePreferences(this);

        Log.d("MyLogs", "Language = " + somePreferences.getVariableLanguage());

        Log.d("MyLogs", "Locale = " + getResources().getConfiguration().locale);
        Log.d("MyLogs", "User's locale = " + (new Locale(somePreferences.getVariableLanguage())));
        setLocale(somePreferences.getVariableLanguage());

        Log.d("MyLogs", "IsLogged = " + somePreferences.getVariableIsLogged());

        Toolbar myToolBar = findViewById(R.id.my_toolbar_2);
        setSupportActionBar(myToolBar);


        buttonWordlist = findViewById(R.id.button_wordlist);
        buttonOpen = findViewById(R.id.button_open);

        @SuppressLint("StaticFieldLeak")
        View.OnClickListener onClickButtonWordlist = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnWordlistButton(v);
            }
        };

        buttonWordlist.setOnClickListener(onClickButtonWordlist);

        View.OnClickListener onClickButtonOpen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click on open_button", Toast.LENGTH_LONG).show();
                clickOnOpenButton();
            }
        };

        buttonOpen.setOnClickListener(onClickButtonOpen);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null || data.getData() == null) {
                    return;
                }

                Uri selectedFileUri = data.getData();

                try {
                    // Получаем InputStream из URI
                    InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);

                    if (inputStream != null) {
                        // Чтение файла ePub
                        EpubReader epubReader = new EpubReader();
                        Book book = epubReader.readEpub(inputStream);

                        if (book != null) {
                            Log.d("MyLogs", "Book Title: " + book.getTitle());
                            Log.d("MyLogs", "Metadata: " + book.getMetadata().getPublishers());
                            Log.d("MyLogs", "Table of Contents Size: " + book.getTableOfContents().size());
                            Log.d("MyLogs", "Contents Size: " + book.getContents().size());

                            // Обработка содержимого книги
                            logTableOfContents(book.getTableOfContents().getTocReferences(), 0);

                            // Запуск активности для отображения книги
                            startActivity(ScreenSlidePagerActivity.getIntent(MainActivity.this, textOfBook));
                        }
                    }
                } catch (IOException e) {
                    Log.e("MyLogs", "Error reading ePub file: " + e.getMessage());
                }
            }
            if (requestCode == OPEN_LOGIN_ACTIVITY) {
                if (data == null) {
                    return;
                }
                SomePreferences somePreferences = new SomePreferences(this);
                somePreferences.setVariableIsLogged(1);
                somePreferences.setToken(data.getStringExtra("token"));
                recreate();
            }
        }
    }

    private void logTableOfContents(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }
        for (TOCReference tocReference : tocReferences) {
            StringBuilder tocString = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                tocString.append("\t");
            }
            try {
                InputStream is = tocReference.getResource().getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = r.readLine()) != null) {
                    line = Html.fromHtml(line).toString();
                    stringBuilder.append(line + " ");
                    //Log.d("Book", line);
                }
                textOfBook = stringBuilder.toString();
            } catch (IOException e) {

            }

            //logTableOfContents(tocReference.getChildren(), depth + 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MyLogs", "Permission OK");
            } else {
                Log.d("MyLogs", "Permission FAILED");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
