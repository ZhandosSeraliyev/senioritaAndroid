package com.example.user.senioritaandroid.Driver.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.user.senioritaandroid.ApiService;
import com.example.user.senioritaandroid.Client.Request;
import com.example.user.senioritaandroid.Constant;
import com.example.user.senioritaandroid.Driver.Adapter.RequestListAdapter;
import com.example.user.senioritaandroid.R;
import com.example.user.senioritaandroid.Refresh;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrentRequestsActivity extends AppCompatActivity implements Refresh {

    ListView listView;
    Context thisContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_requests);
        thisContext = this;
        listView = (ListView) findViewById(R.id.list_view);
        getRequests(thisContext);
    }

    public boolean getRequests(final Context context) {
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                if (chain.request().header("noToken") == "true") {
                    return chain.proceed(chain.request());
                }
                SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
                String token = preferences.getString("token","");
                Log.v("Token", token);
                okhttp3.Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer "+token).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Single<ArrayList<Request>> requests = apiService.getRequests();
        requests.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ArrayList<Request>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("disposable", d.toString());
                    }
                    @Override
                    public void onSuccess(ArrayList<Request> requests) {
                        Log.v("RequestDriver:", requests.toString());
                        RequestListAdapter adapter = new RequestListAdapter(context, R.layout.adapter_view, requests);
                        listView.setAdapter(adapter);
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", e.toString());
                    }
                });
        return true;
    }

    @Override
    public void yourDesiredMethod() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
