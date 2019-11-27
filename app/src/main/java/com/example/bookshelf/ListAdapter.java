package com.example.bookshelf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    ArrayList<Book> bookCollection;
    Context c;

    public ListAdapter(Context c, ArrayList<Book> bookCollection){
        this.bookCollection = bookCollection;
        this.c = c;
    }

    @Override
    public int getCount() {
        return bookCollection.size();
    }

    @Override
    public Object getItem(int position) {
        return bookCollection.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v;
        if (convertView instanceof  View){
            v = (TextView)convertView;
        } else {
            v = new TextView(c);
        }
        v.setTextSize(30);
        v.setText(bookCollection.get(position).title);
        return v;
    }
}
