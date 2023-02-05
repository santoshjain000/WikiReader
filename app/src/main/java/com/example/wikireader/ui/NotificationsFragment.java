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
import com.example.wikireader.databinding.FragmentNotificationsBinding;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationsFragment extends Fragment {


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

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initializing component's
        final TextView textView = binding.messagearea;
        Loading = binding.Loading;
        progressBar = binding.progress;
        messagearea = binding.messagearea;
        recyclerView = binding.listViewRepos;


        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        context = getActivity();

        // Initializing database
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



        //Calling API Data using Volley request
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
        binding = null;
    }


    public void Call_server(String continue_variable) {
        try {
            final String[] data_of_continue = {""};
            progressBar.setVisibility(View.VISIBLE);
            RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());
            String url = "https://en.wikipedia.org/w/api.php?action=query&list=allcategories&acprefix=&format=json"+continue_variable;//Helpers.getappUrl(this); // <----enter your post url here

            StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject parentArray = new JSONObject(response);
                        dbManager.open();
                        for (int i = 0; i < parentArray.length(); i++) {
                            JSONObject row = parentArray.getJSONObject("query");
                            JSONObject continue_row = parentArray.getJSONObject("continue");
                            try
                            {
                                JSONArray myListsAll = new JSONArray(row.getString("allcategories"));
                                for (int ai = 0; ai < myListsAll.length(); ai++) {
                                    JSONObject jsonobject = (JSONObject) myListsAll.get(ai);
                                    if (CheckInternet.isConnectionAvailable(context)) {
                                        articles_Model.add(new articles_Model(
                                                Integer.valueOf(ai),
                                                jsonobject.getString("*"),
                                                "",
                                                "",
                                                "", 0));
                                    }
                                    if(dbManager.GetVbyID_Sync(String.valueOf(ai)).getCount()>0){
                                    }else {
                                        dbManager.insert(jsonobject.getString("*"), "", String.valueOf(ai),
                                                "", getDate(),getTime(), "1","CAT");
                                    }
                                }
                            }
                            catch (JSONException e)
                            {   e.printStackTrace();    }
                            data_of_continue[0] = "&accontinue=" + String.valueOf(continue_row.getString("accontinue")) + "&continue=" + String.valueOf(continue_row.getString("continue"));
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
            Cursor cursor = dbManager.GetAll("CAT");
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