package com.example.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListFragmentInterface, BookPagerFragment.BookPagerInterface, BookTitleFragment.BookTitleInterface, BookDetailFragment.BookDetailInterface {
    static ArrayList<Book> bookCollection = new ArrayList<>();
    static int currentDisplayedBook = 0;
    private JSONArray bookJSON;
    private String apiURL = "https://kamorris.com/lab/audlib/booksearch.php";
    private String apiURLs = "https://kamorris.com/lab/audlib/booksearch.php?search=";
    private int currentProgress;
    private Book playingBook;
    private String currentStatus;
    private DownloadManager downloadManager;

    Handler getBookHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                bookJSON = new JSONArray(msg.obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            initializeBook(bookJSON);
            //TODO: if this succeed then assign to static bookCollection --> use temp collection
            if (bookCollection.size() > 0) {
                currentDisplayedBook = 0;
                FrameLayout detailContainer = findViewById(R.id.detailContainer);

                if (detailContainer instanceof FrameLayout) { //either landscape or big screen
                    BookListFragment fragment1 = (BookListFragment) getSupportFragmentManager().findFragmentByTag("bookListFragment");

                    if (fragment1 == null) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.fragmentContainer, BookListFragment.newInstance(bookCollection), "bookListFragment")
                                .commit();
                    } else {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .remove(fragment1)
                                .add(R.id.fragmentContainer, BookListFragment.newInstance(bookCollection), "bookListFragment")
                                .commit();
                    }
                } else { //single panel display
                    BookPagerFragment fragment1 = (BookPagerFragment)getSupportFragmentManager().findFragmentByTag("bookPagerFragment");
                    //
                    if (fragment1 == null) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.pagerContainer, BookPagerFragment.newInstance(bookCollection, 0), "bookPagerFragment")
                                .commit();
                    } else {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .remove(fragment1)
                                .replace(R.id.pagerContainer, BookPagerFragment.newInstance(bookCollection, 0), "bookPagerFragment")
                                .commit();
                    }
                }
                checkFile();
            }
            return false;
        }
    });

    Handler getAudioProgress = new Handler(msg -> {
        AudiobookService.BookProgress obj = (AudiobookService.BookProgress) msg.obj;
        SeekBar seekBar = findViewById(R.id.seekBar);
        if (obj != null) {
            seekBar.setProgress(obj.getProgress());
            currentProgress = obj.getProgress();
        }
        return false;
    });

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (AudiobookService.MediaControlBinder) service;
            binder.setProgressHandler(getAudioProgress);
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    AudiobookService.MediaControlBinder binder;
    boolean mServiceBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        currentStatus = savedInstanceState.getString("statusView");
        playingBook = (Book)savedInstanceState.getSerializable("book");
        currentDisplayedBook = savedInstanceState.getInt("currentDisplayedBook");
        bookCollection = (ArrayList<Book>)savedInstanceState.getSerializable("bookCollection");
        if (playingBook != null) {
            SeekBar seekBar = findViewById(R.id.seekBar);
            seekBar.setMax(playingBook.duration);
        }
        TextView status = findViewById(R.id.statusTextView);
        status.setText(currentStatus);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("statusView", currentStatus);
        savedInstanceState.putSerializable("book", playingBook);
        savedInstanceState.putSerializable("bookCollection",bookCollection);
        savedInstanceState.putInt("currentDisplayedBook",currentDisplayedBook);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound){
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        ArrayList<Book> bookCol = new ArrayList<>();

        FrameLayout pagerContainer = findViewById(R.id.pagerContainer);
        if (pagerContainer instanceof FrameLayout) { //if portrait
            BookListFragment fragment = (BookListFragment) getSupportFragmentManager().findFragmentByTag("bookListFragment");
            if (fragment != null) {
                bookCol = fragment.getBook();
                if (bookCol.isEmpty() && !bookCollection.isEmpty()){
                    bookCol = bookCollection;
                }
            }
            BookPagerFragment fragment1 = (BookPagerFragment) getSupportFragmentManager().findFragmentByTag("bookPagerFragment");
            if (fragment1 != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment1)
                        .commit();

            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.pagerContainer, BookPagerFragment.newInstance(bookCol, currentDisplayedBook), "bookPagerFragment")
                    .commit();
            checkFile();
        } else { //if landscape
            BookPagerFragment fragment = (BookPagerFragment) getSupportFragmentManager().findFragmentByTag("bookPagerFragment");
            if (fragment != null) {
                bookCol = fragment.getBook();
                if (bookCol.isEmpty() && !bookCollection.isEmpty()){
                    bookCol = bookCollection;
                }
            }
            BookListFragment fragment1 = (BookListFragment) getSupportFragmentManager().findFragmentByTag("bookListFragment");
            //TODO: This will be NULL --> Fix
            if (fragment1 != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment1)
                        .commit();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, BookListFragment.newInstance(bookCol), "bookListFragment")
                    .commit();
            if (!bookCollection.isEmpty()) {
                onBookSelected(currentDisplayedBook, bookCollection.get(currentDisplayedBook));
            }
        }


        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> new Thread() {
            @Override
            public void run() {
                try {
                    URL url;
                    EditText searchBox = findViewById(R.id.searchBox);
                    if (searchBox.getText().toString().isEmpty()){
                        url = new URL(apiURL);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(apiURLs);
                        sb.append(searchBox.getText().toString());
                        url = new URL(sb.toString());
                    }
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));

                    StringBuilder response = new StringBuilder();
                    String tmpResponse;

                    while ((tmpResponse = reader.readLine()) != null) {
                        response.append(tmpResponse);
                    }

                    Message msg = Message.obtain();

                    msg.obj = response.toString();

                    Log.d("Books RECEIVED", response.toString());

                    getBookHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start());

        //PAUSE BUTTON HANDLER
        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener((v -> {
            int savedProgress;
            if (currentProgress <= 10){
                savedProgress = 0;
            } else {
                savedProgress = currentProgress - 10;
            }
            if (binder.isPlaying()) {
                try {
                    //Save progress to storage
                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BookShelfData");
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    File file = new File(root, "book" + playingBook.id + ".txt");
                    FileWriter writer = new FileWriter(file);
                    writer.append(String.valueOf(savedProgress));
                    writer.flush();
                    writer.close();
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                binder.stop();
                TextView statusTextView = findViewById(R.id.statusTextView);
                statusTextView.setText(playingBook.title + " paused");
                currentStatus = statusTextView.getText().toString();
            }
//            } else if (playingBook != null){
//                playBook(playingBook);
//                TextView statusTextView = findViewById(R.id.statusTextView);
//                statusTextView.setText(playingBook.title + " playing");
//                currentStatus = statusTextView.getText().toString();
//                pauseButton.setText("PAUSE");
//
//            }
        }));
        //STOP BUTTON HANDLER
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener((v -> {
            //Delete progress file from storage
            if (playingBook != null) {
                String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/BookShelfData/" + "book" + playingBook.id + ".txt";
                File file = new File(fpath);
                if (file.exists()) {
                    file.delete();
                }
                binder.stop();
                playingBook = null;
                TextView statusTextView = findViewById(R.id.statusTextView);
                statusTextView.setText("");
                currentStatus = statusTextView.getText().toString();
                SeekBar seekBar = findViewById(R.id.seekBar);
                seekBar.setProgress(0);
            }
        }));

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    binder.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> {
            if (!bookCollection.isEmpty()) {
                downloadThis(bookCollection.get(currentDisplayedBook));
            }
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            if (currentDisplayedBook < bookCollection.size()) {
                deleteThis(bookCollection.get(currentDisplayedBook));
            }
        });
    }

    public void initializeBook(JSONArray js){
        if (js.length() > 0) {
            bookCollection.clear();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Can't find requested book",
                    Toast.LENGTH_LONG).show();
        }
        for (int i = 0; i < js.length(); i++){
            try {
                JSONObject e = js.getJSONObject(i);
                Book b = new Book(
                        e.getInt("book_id"),
                        e.getString("title"),
                        e.getString("author"),
                        e.getInt("published"),
                        e.getString("cover_url"),
                        e.getInt("duration")
                );
                bookCollection.add(b);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        currentDisplayedBook = 0;
        checkFile();
    }

    @Override
    public void onBookSelected(int position, Book theBook) {
        currentDisplayedBook = position;
        Log.v("position",String.valueOf(theBook.id));
        BookDetailFragment fragment = (BookDetailFragment) getSupportFragmentManager().findFragmentByTag("bookDetailFragment");
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.detailContainer, BookDetailFragment.newInstance(theBook), "bookDetailFragment")
            .commit();
        checkFile();
    }

    @Override
    public void onPageSelect(int position) {
        currentDisplayedBook = position;
        checkFile();
    }

    public void checkFile(){
        Log.v("test",currentDisplayedBook + " " + bookCollection.size());
        if (currentDisplayedBook < bookCollection.size()) {
            Log.v("position","from checkfile"+bookCollection.get(currentDisplayedBook).id);
            String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/book" + bookCollection.get(currentDisplayedBook).id + ".mp3";
            File file = new File(fpath);
            if (file.isFile()) {
                Log.v("file","find found");
                Button downloadButton = findViewById(R.id.downloadButton);
                downloadButton.setVisibility(View.INVISIBLE);
                Button deleteButton = findViewById(R.id.deleteButton);
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                Button downloadButton = findViewById(R.id.downloadButton);
                downloadButton.setVisibility(View.VISIBLE);
                Button deleteButton = findViewById(R.id.deleteButton);
                deleteButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void playBook(Book book) {
        //Save playing book
        if (binder.isPlaying()) {
            try {
                int savedProgress;
                if (currentProgress <= 10) {
                    savedProgress = 0;
                } else {
                    savedProgress = currentProgress - 10;
                }
                //Save progress to storage
                File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BookShelfData");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File file = new File(root, "book" + playingBook.id + ".txt");
                FileWriter writer = new FileWriter(file);
                writer.append(String.valueOf(savedProgress));
                writer.flush();
                writer.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Play the requested book
        playingBook = book;
        //Check if audio file exists
        boolean audio = false;
        String a_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/book" + book.id + ".mp3";
        File a_file = new File(a_path);
        if (a_file.exists()){
            audio = true;
        }
        //check if progress file exits
        boolean progress = false;
        String p_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/BookShelfData/book" + book.id + ".txt";
        File p_file = new File(p_path);
        StringBuilder text = new StringBuilder();
        if (p_file.exists()){
            progress = true;
            try {
                BufferedReader br = new BufferedReader(new FileReader(p_file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }
            Toast.makeText(this,"Found progress for book" + book.id,Toast.LENGTH_LONG).show();
        }
        //Play book
        if (mServiceBound) {
            //If file exist
            if (audio) {
                if (progress) {
                    binder.play(a_file, Integer.valueOf(String.valueOf(text)));
                } else {
                    binder.play(a_file);
                }
                Toast.makeText(this, "PLaying from file", Toast.LENGTH_LONG).show();
            } else {
                binder.play(playingBook.id, 0);
                Toast.makeText(this, "PLaying from server", Toast.LENGTH_LONG).show();
            }
        }

        TextView statusTextView = findViewById(R.id.statusTextView);
        statusTextView.setText(playingBook.title + " playing");
        currentStatus = statusTextView.getText().toString();
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(playingBook.duration);
    }

    public void downloadThis(Book theBook){
        String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/book" + theBook.id + ".mp3";
        File file = new File(fpath);
        if (!file.isFile()) {
            new DownloadBook().execute(theBook);
            Button downloadButton = findViewById(R.id.downloadButton);
            downloadButton.setVisibility(View.INVISIBLE);
            Button deleteButton = findViewById(R.id.deleteButton);
            deleteButton.setVisibility(View.VISIBLE);
            //Disable download and activate delete
        }
    }

    public void deleteThis(Book theBook){
        String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/book" + bookCollection.get(currentDisplayedBook).id + ".mp3";
        File file = new File(fpath);
        if (file.isFile()){
            file.delete();
        }
        checkFile();
    }

    private class DownloadBook extends AsyncTask<Book, String, String>{
       @Override
        protected String doInBackground(Book... theBook) {
           try {
               Log.v("download", "downloading");
               String s_url = "https://kamorris.com/lab/audlib/download.php?id=" + theBook[0].id;

               downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
               Uri uri = Uri.parse(s_url);
               DownloadManager.Request request = new DownloadManager.Request(uri);
               request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
               request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"book"+theBook[0].id+".mp3");
               Log.v("path",Environment.DIRECTORY_DOWNLOADS);
               request.setMimeType("audio/mpeg");
               downloadManager.enqueue(request);
           } catch (Exception e){
               Log.e("Error",e.getMessage());
           }
           return null;
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(getApplicationContext(),
                    "Download finished!", Toast.LENGTH_LONG).show();
        }
    }
}
