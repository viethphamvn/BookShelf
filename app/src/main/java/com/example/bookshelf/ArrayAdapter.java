package com.example.bookshelf;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ArrayAdapter extends BaseAdapter implements ListAdapter {
    Context context;
    ArrayList<Book> bookCollection;

    public ArrayAdapter(ArrayList<Book> bookCollection, Context context){
        this.bookCollection = bookCollection;
        this.context = context;
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
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.my_custom_adapter_layout, null);
        }

        TextView titleTextView = view.findViewById(R.id.titleView);
        titleTextView.setText(bookCollection.get(position).title);

        Button playButton = view.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("played", String.valueOf(bookCollection.get(position).id));
                ((MainActivity)context).playBook(bookCollection.get(position));
                ((MainActivity)context).onBookSelected(position, bookCollection);
            }
        });

        return view;
    }

    public interface ArrayAdapterInterface{
        void playBook(Book theBook);
    }
}
