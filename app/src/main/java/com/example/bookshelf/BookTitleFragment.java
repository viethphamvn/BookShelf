package com.example.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bookshelf.Book;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BookTitleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private Book theBook;
    private BookTitleInterface parentFragment;

    public BookTitleFragment() {
        // Required empty public constructor
    }

    public static BookTitleFragment newInstance(Book theBook) {
        BookTitleFragment fragment = new BookTitleFragment();
        Bundle args = new Bundle();
        args.putSerializable("theBook", theBook);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            theBook = new Book((Book)getArguments().getSerializable("theBook"));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookTitleInterface) {
            parentFragment = (BookTitleInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_title, container, false);
        TextView titleView = v.findViewById(R.id.titleView);
        TextView authorView = v.findViewById(R.id.authorView);
        TextView publishedView = v.findViewById(R.id.publishedView);

        titleView.setText("Title: " + theBook.title);
        authorView.setText("Author" + theBook.author);
        publishedView.setText("Published on: " + theBook.published);
        Picasso.get().load(theBook.coverURL).into((ImageView)v.findViewById(R.id.coverView));

        Button playButton = v.findViewById(R.id.playButton);
        playButton.setOnClickListener(v1 -> {
            Log.v("played", String.valueOf(theBook.id));
            parentFragment.playBook(theBook);
        });

        return v;
    }

    public interface BookTitleInterface{
        void playBook(Book book);
    }

}
