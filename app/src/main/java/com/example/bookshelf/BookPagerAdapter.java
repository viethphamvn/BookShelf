package com.example.bookshelf;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.bookshelf.Book;

import java.util.ArrayList;

public class BookPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Book> bookCollection;

    public BookPagerAdapter(@NonNull FragmentManager fm, ArrayList<Book> bookCollection) {
        super(fm);
        this.bookCollection = bookCollection;

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        BookTitleFragment fragment = BookTitleFragment.newInstance(bookCollection.get(position));
        return fragment;
    }

    @Override
    public int getCount() {
        return bookCollection.size();
    }
}
