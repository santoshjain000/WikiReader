package com.example.wikireader.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.wikireader.Model.articles_Model;
import com.example.wikireader.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.bliki.wiki.model.WikiModel;

public class article_Adapter extends RecyclerView.Adapter<article_Adapter.articles_ModelViewHolder>{

    private Context mContext;
    ArrayList<articles_Model> articles_Model;

    public article_Adapter(Context context, ArrayList<articles_Model>  articles_Model){

        this.articles_Model = articles_Model;
        this.mContext = context;

    }


    @Override
    public articles_ModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_article, parent, false);
        return new articles_ModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(articles_ModelViewHolder holder, int position) {
        holder.textRepoName.setText(articles_Model.get(position).name);

        WikiModel wikiModel =
                new WikiModel("https://www.mywiki.com/wiki/${image}",
                        "https://www.mywiki.com/wiki/${title}");
        String htmlStr = null;
        try {
            htmlStr = wikiModel.render(articles_Model.get(position).description);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String a = htmlStr;
        a = a.replace("{{","");
        a = a.replace("}}","");
        a = a.replace("File:","");
        a = a.replace(".jpg","");
        a = a.replace(".svg","");
        a = a.replace(".PNG","");
        a = a.replace(".JPG","");
        a = a.replace(".gif","");
        a = a.replace(".png","");
        holder.textRepoDescription.setText(Html.fromHtml(a));
        //  holder.textLanguage.setText("Language: " + article_Data_Model.get(position).thumb_Url);
        holder.textStars.setText("Stars: " + articles_Model.get(position).stargazersCount);


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.articles)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform();

        Glide.with(mContext).load(articles_Model.get(position).thumb_Url)
                .apply(options).into(holder.thumb);
//
//        Glide.with(mContext)
//                .load(article_Data_Model.get(position).thumb_Url)
//                .placeholder(R.drawable.ic_launcher_foreground)
//                .into(holder.thumb);

    }

    public void setarticle_Data_Model(@Nullable List<articles_Model> repos) {
        if (repos == null) {
            return;
        }
        articles_Model.clear();
        articles_Model.addAll(repos);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return articles_Model.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class articles_ModelViewHolder extends RecyclerView.ViewHolder {

        TextView textRepoName;
        TextView textRepoDescription;
        TextView textLanguage;
        TextView textStars;
        ImageView thumb;

        public articles_ModelViewHolder(View itemView) {
            super(itemView);


            textRepoName = (TextView) itemView.findViewById(R.id.text_repo_name);
            textRepoDescription = (TextView) itemView.findViewById(R.id.text_repo_description);
            textLanguage = (TextView) itemView.findViewById(R.id.text_language);
            textStars = (TextView) itemView.findViewById(R.id.text_stars);
            thumb = (ImageView) itemView.findViewById(R.id.thumb);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });



        }
    }

}
