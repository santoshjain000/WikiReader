package com.example.wikireader.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.wikireader.Adapter.article_Adapter;
import com.example.wikireader.Database.DBHandler;
import com.example.wikireader.Model.articles_Model;
import com.example.wikireader.R;
import com.example.wikireader.Support.CheckInternet;
import com.example.wikireader.databinding.FragmentHomeBinding;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    ArrayList<articles_Model> articles_Model= new ArrayList();
    private LinearLayoutManager mLinearLayoutManager;
    article_Adapter recyclerAdapter;
    RecyclerView recyclerView;

    ShimmerFrameLayout Loading;


    private ProgressBar progressBar;
    private TextView messagearea;
    private Disposable subscription;

    String continue_variable="";
    DBHandler dbManager;
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.messagearea;
        Loading = binding.Loading;
        progressBar = binding.progress;
        messagearea = binding.messagearea;
        recyclerView = binding.listViewRepos;


        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        context = getActivity();

        dbManager = new DBHandler(context);

        progressBar.setVisibility(View.VISIBLE);
        Loading.setVisibility(View.VISIBLE);
        messagearea.setVisibility(View.GONE);

        recyclerAdapter = new article_Adapter(getContext(), articles_Model);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager=LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
                if (totalItemCount > 0 && endHasBeenReached) {
                    //you have reached to the bottom of your recycler view
                    progressBar.setVisibility(View.VISIBLE);

                    mLinearLayoutManager.scrollToPosition(articles_Model.size());
                    if (CheckInternet.isConnectionAvailable(context)) {

                        Call_server(continue_variable);
                    }else{
                        SetDataToadapter(continue_variable);
                    }
                }
            }
        });

        if (CheckInternet.isConnectionAvailable(context)) {

            Call_server(continue_variable);

        }else{
            SetDataToadapter(continue_variable);
        }
        Loading.setVisibility(View.VISIBLE);
        Loading.startShimmerAnimation();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;}


        public void Call_server(String continue_variable) {
            try {
                final String[] data_of_continue = {""};
                progressBar.setVisibility(View.VISIBLE);
                RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());
                String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&generator=random&grnnamespace=0&prop=revisions%7Cimages&rvprop=content&grnlimit=10"+continue_variable;//Helpers.getappUrl(this); // <----enter your post url here
                StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response",response);
                        try {
                            JSONObject parentArray = new JSONObject(response);
                            dbManager.open();
                            for (int i = 0; i < parentArray.length(); i++) {
                                JSONObject row = parentArray.getJSONObject("query");
                                JSONObject continue_row = parentArray.getJSONObject("continue");
                                String desc = "";
                                try
                                {
                                    JSONObject jObject= row.getJSONObject("pages");
                                    Iterator<String> keys = jObject.keys();
                                    while( keys.hasNext() ) {
                                        String key = keys.next();
                                        JSONObject innerJObject = jObject.getJSONObject(key);
                                        if(innerJObject.has("revisions")){
                                            JSONArray myListsAll = new JSONArray(innerJObject.getString("revisions"));
                                            for (int ai = 0; ai < myListsAll.length(); ai++) {
                                                JSONObject jsonobject = (JSONObject) myListsAll.get(ai);
                                                desc = jsonobject.optString("*").substring(0, Math.min(jsonobject.optString("*").length(), 100));
                                                if (dbManager.GetVbyID_Sync(innerJObject.getString("pageid")).getCount() > 0) {
                                                } else {
                                                    dbManager.insert(innerJObject.getString("title"), desc, innerJObject.getString("pageid"),
                                                            desc, getDate(), getTime(), "1", "ART");
                                                }
                                            }
                                        }
                                        if (CheckInternet.isConnectionAvailable(context)) {
                                            articles_Model.add(new articles_Model(
                                                    Integer.valueOf(innerJObject.getString("pageid")),
                                                    innerJObject.getString("title"),
                                                    "",
                                                    desc,
                                                    "", 0));
                                        }
                                    }
                                }

                                catch (JSONException e)
                                {   e.printStackTrace();
                                }
                                if(continue_row.has("imcontinue")){
                                    data_of_continue[0] = "&grncontinue="+String.valueOf(continue_row.getString("grncontinue"))+"&continue="+String.valueOf(continue_row.getString("continue")+"&imcontinue="+String.valueOf(continue_row.getString("imcontinue")));
                                }else {
                                    data_of_continue[0] = "&grncontinue=" + String.valueOf(continue_row.getString("grncontinue")) + "&continue=" + String.valueOf(continue_row.getString("continue"));
                                }
                            }

                            SetDataToadapter(data_of_continue[0]);
                            dbManager.close();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Loading.setVisibility(View.GONE);
                        }

                    }
                }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Loading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Data not loaded, please try after sometime!", Toast.LENGTH_LONG).show();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        return MyData;
                    }
                };

                MyRequestQueue.add(MyStringRequest);
            } catch (Exception ex) {
                progressBar.setVisibility(View.GONE);
                Loading.setVisibility(View.GONE);
                ex.printStackTrace();   Toast.makeText(getContext(), "Data not loaded, please try after sometime!", Toast.LENGTH_LONG).show();
            }
        }

        private String getDate() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault());
            Date date = new Date();
            return dateFormat.format(date);
        }

        private String getTime() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            return dateFormat.format(date);
        }

        private void SetDataToadapter(String data_of_continue) {
            if (!CheckInternet.isConnectionAvailable(context)) {
                dbManager.open();
                Cursor cursor = dbManager.GetAll("ART");
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        articles_Model.add(new articles_Model(
                                Integer.valueOf(String.valueOf(cursor.getString(3))),
                                String.valueOf(cursor.getString(1)),
                                "",
                                String.valueOf(cursor.getString(2)),
                                String.valueOf(cursor.getString(4)), 0));
                    }
                }

                dbManager.close();
            }
            continue_variable = data_of_continue;
            Observable.just(articles_Model)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new io.reactivex.Observer<ArrayList>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Loading.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onNext(ArrayList arrayList) {
                            recyclerAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onError(Throwable e) {
                            //error handling made simple
                            messagearea.setText(messagearea.getText().toString() +"\n" +"OnError" );
                            progressBar.setVisibility(View.INVISIBLE);
                            Loading.stopShimmerAnimation();
                            Loading.setVisibility(View.GONE);
                            messagearea.setText(messagearea.getText().toString() +"\n" +"Hidding Progressbar" );
                        }
                        @Override
                        public void onComplete() {
                            Loading.stopShimmerAnimation();
                            Loading.setVisibility(View.GONE);
                            messagearea.setText(messagearea.getText().toString() +"\n" +"OnComplete" );
                            progressBar.setVisibility(View.INVISIBLE);
                            messagearea.setText(messagearea.getText().toString() +"\n" +"Hidding Progressbar" );
                        }
                    });

        };
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        }
}