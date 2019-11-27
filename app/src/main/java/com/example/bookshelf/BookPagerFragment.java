package com.example.bookshelf;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookshelf.Book;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookPagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "bookCollection";
    private static final String ARG_PARAM2 = "currentItem";
    private BookPagerInterface parentFragment;
    // TODO: Rename and change types of parameters
    public ArrayList<Book> bookCollection;
    private int currentItem;

    public BookPagerFragment() {
        // Required empty public constructor
    }

    public static BookPagerFragment newInstance(ArrayList<Book> bookCollection, int currentItem) {

        BookPagerFragment fragment = new BookPagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, bookCollection);
        args.putInt(ARG_PARAM2, currentItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookCollection = (ArrayList<Book>) getArguments().getSerializable(ARG_PARAM1);
            currentItem = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookPagerInterface) {
            parentFragment = (BookPagerInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_pager, container, false);
        ViewPager viewPager = v.findViewById(R.id.viewPager);
        BookPagerAdapter pagerAdapter = new BookPagerAdapter(getChildFragmentManager(), bookCollection);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentItem, true);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                parentFragment.onPageSelect(position);
                Log.v("real",String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    public ArrayList<Book> getBook(){
        return bookCollection;
    }

    public interface BookPagerInterface {
        // TODO: Update argument type and name
        void onPageSelect(int position);
    }

}
