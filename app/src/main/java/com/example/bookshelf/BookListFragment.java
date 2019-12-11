package com.example.bookshelf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class BookListFragment extends Fragment {
    private static final String ARG_PARAM1 = "bookCollection";

    // TODO: Rename and change types of parameters
    public ArrayList<Book> bookCollection;

    private BookListFragmentInterface parentFragment;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(ArrayList<Book> bookCollection) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, bookCollection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookCollection = (ArrayList<Book>)getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);
        ListView listView = v.findViewById(R.id.listView);

        //ArrayAdapter listAdapter = new ArrayAdapter(bookCollection, getActivity());
        ListAdapter listAdapter = new ListAdapter(getActivity(), bookCollection);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> parentFragment.onBookSelected(position, bookCollection.get(position)));
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookListFragmentInterface) {
            parentFragment = (BookListFragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public ArrayList<Book> getBook(){
        return bookCollection;
    }

    public interface BookListFragmentInterface {
        // TODO: Update argument type and name
        void onBookSelected(int position, Book theBook);
    }
}
