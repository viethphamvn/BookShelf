package com.example.bookshelf;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bookshelf.Book;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailFragment extends Fragment {
    private static final String ARG_PARAM1 = "theBook";
    private Book theBook;
    private View v;

    public BookDetailFragment() {
        // Required empty public constructor
    }

    public static BookDetailFragment newInstance(Book theBook) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, theBook);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            theBook = (Book)getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_book_detail, container, false);
        if (getArguments() != null) {
            TextView titleView = v.findViewById(R.id.titleView);
            TextView authorView = v.findViewById(R.id.authorView);
            TextView publishedView = v.findViewById(R.id.publishedView);
            ImageView coverView = v.findViewById(R.id.coverView);

            titleView.setText("Title: " + theBook.title);
            authorView.setText("Author" + theBook.author);
            publishedView.setText("Published on: " + theBook.published);
            Picasso.get().load(theBook.coverURL).into((ImageView)v.findViewById(R.id.coverView));
        }

        return v;
    }

    public void displayBook(Book theBook){
        TextView titleView = v.findViewById(R.id.titleView);
        TextView authorView = v.findViewById(R.id.authorView);
        TextView publishedView = v.findViewById(R.id.publishedView);
        ImageView coverView = v.findViewById(R.id.coverView);

        titleView.setText("Title: " + theBook.title);
        authorView.setText("Author" + theBook.author);
        publishedView.setText("Published on: " + theBook.published);
        Picasso.get().load(theBook.coverURL).into((ImageView)v.findViewById(R.id.coverView));
    }


}
