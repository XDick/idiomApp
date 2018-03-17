package com.example.administrator.havingdate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class Activity2 extends Fragment {


    private SwipeRefreshLayout swipeRefresh;

   private  Document document2;
   private  Document document;
    private List<Idiom> idiomList = new ArrayList<>();
    private  Idiom[] idiomArry;
    private IdiomAdapter adapter;
    private Elements sizeElements;
    private int ListSize;
    private View rootView;//缓存Fragment view



    /*--------------------------------------------------------*/
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView==null) {
            rootView = inflater.inflate(R.layout.fragment_layout2, container, false);
        }
       //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。

            ViewGroup parent = (ViewGroup) rootView.getParent();

            if (parent != null) {

                parent.removeView(rootView);
            }

    Log.d(TAG,"看看有没有运行");
        Connector.getDatabase();
        getHtmlFromJsoup();


/*------------------------------------数据库储存-----------------------*/

          initIdioms();

/*-----------------------------列表--------------------------------------*/
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new IdiomAdapter(idiomList);
        recyclerView.setAdapter(adapter);
        Log.d(TAG,"列表生成的代码");

        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshInformations();
            }
        });


        return rootView;
    }
    /*--------------------------实现刷新功能---------------------------*/

    private void refreshInformations() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ((AppCompatActivity) getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initIdioms();
                        Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
    /*------------------------------------------------------------------------*/







/*--------------------------Jsoup爬虫--------------------------------------------------*/

    private void getHtmlFromJsoup(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    for (int j = 1; j <= 999; j++) {
                        document = Jsoup.
                                connect("http://www.gs5000.cn/gs/chengyu/list_5_" + j + ".html")
                                .timeout(50000).get();

                        Elements titleElements = document.getElementsByClass("title");
                        Elements bodyElements = document.getElementsByClass("intro");
                        Elements imgElements = document.getElementsByClass("preview");
                        sizeElements = document.getElementsByClass("preview");
                        Elements contentElements = document.getElementsByClass("preview");


                        Log.d(TAG, "title:" + titleElements
                                .select("a").text());
                        Log.d(TAG, bodyElements.text());
                        Log.d(TAG, "pic:" + "http://www.gs5000.cn" + imgElements.select("img").attr("src"));
                        Log.d(TAG, "content:" + "http://www.gs5000.cn" + contentElements.select("a").attr("href"));


                            for (int i = 0; i < (sizeElements.size()); i++) {
                                document2 = Jsoup.
                                        connect("http://www.gs5000.cn"
                                                + contentElements.get(i).attr("href"))
                                        .timeout(0).get();

                                Elements contentElements2 = document2.getElementsByClass("content");
                               Idiom idioms = new Idiom(titleElements.get(i + 1)
                                        .select("a").text(), bodyElements.get(i).text()
                                        , "http://www.gs5000.cn" + imgElements.get(i)
                                        .select("img")
                                        .attr("src")
                                        , contentElements2.select("table").text());
                                  idioms.save();
                                System.out.print(sizeElements.size());
                                if (sizeElements.size() < 10) {
                                    break;
                                }

                            }

                            }
                              Log.d(TAG , "加载完毕！");
                    }
                catch(Exception e){
                        e.printStackTrace();
                        Log.d(TAG, "访问网络失败了！");


                }

                }
        }).start();
    }





    private void initIdioms() {
        idiomList.clear();
        List<Idiom> idiomData = new ArrayList<Idiom>();
        idiomData = DataSupport.limit(1000).offset(0).find(Idiom.class);
        Collections.shuffle(idiomData);//使列表乱序
        for (Idiom idiom: idiomData){

            idiomList.add(idiom);
            Log.d(TAG , idiom.getTitle());
        }
        ListSize = idiomData.size();
        Log.d(TAG , "列表大小"+ListSize);

   }
    }