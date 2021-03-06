package com.example.user.senioritaandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.senioritaandroid.User.Token;
import com.example.user.senioritaandroid.User.User;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;
    Button loginB, registerB;
    EditText userNameE, passwordE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginB = (Button) findViewById(R.id.button);
        userNameE = (EditText)findViewById(R.id.editText);
        passwordE = (EditText)findViewById(R.id.editText2);
        registerB = (Button) findViewById(R.id.buttonR);
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    login(userNameE.getText().toString(), passwordE.getText().toString());
            }
        });
        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    register();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
                    startActivity(splashIntent);
                    finish();
            }
        }, SPLASH_TIME_OUT);

    }

    public boolean register() {
        Intent register = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(register);
        return true;
    }

    public Boolean login(String userName, String password) {
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                if (chain.request().header("noToken") == "true") {
                    return chain.proceed(chain.request());
                }
                SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
                String token = preferences.getString("token","");
                Request newRequest = chain.request().newBuilder().addHeader("Authorization","Bearer "+token).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Single<Token> token = apiService.loginAccount("password", userName, password, getAuthorizationHeader(), "application/x-www-form-urlencoded; charset=utf-8",true);
        token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Token>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("DISPOSABLE:", d.toString());
                    }

                    @Override
                    public void onSuccess(Token token) {
                        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
                        preferences.edit().putString("token", token.getAccessToken()).commit();
                        Log.v("SUCCESS:", token.toString());
                        redirectDriverClient();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            ResponseBody responseBody = ((HttpException)e).response().errorBody();
                            Log.e("ERRORLogin", e.toString());
                            Log.e("ERRORLogin", responseBody.toString());
                        } else if (e instanceof SocketTimeoutException) {
                            Log.e("ERRORLogin", "SocketTimeout");
                        } else if (e instanceof IOException) {
                            Log.e("ERRORLogin", "IOE " +   e);
                        } else {
                            Log.e("ERRORLogin", "UNK");
                        }
                    }
                });
        return true;
    }

    public void redirectDriverClient() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                if (chain.request().header("noToken") == "true") {
                    return chain.proceed(chain.request());
                }
                SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
                String token = preferences.getString("token","");
                Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer "+token).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Single<User> user = apiService.getUser();
        user.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("DISPOSABLE:", d.toString());
                    }

                    @Override
                    public void onSuccess(User user) {
                        Log.v("User:", user.toString());
                        if (user.getRole().getId()==2) {
                            Intent role = new Intent(MainActivity.this, HomeClientActivity.class);
                            startActivity(role);
                        } else {
                            Intent role = new Intent(MainActivity.this, HomeDriverActivity.class);
                            startActivity(role);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            ResponseBody responseBody = ((HttpException)e).response().errorBody();
                            Log.e("ERRORMAIN", e.toString());
                            Log.e("ERRORMAIN", responseBody.toString());
                        } else if (e instanceof SocketTimeoutException) {
                            Log.e("ERRORMAIN", "SocketTimeout");
                        } else if (e instanceof IOException) {
                            Log.e("ERRORMAIN", "IOE " +   e);
                        } else {
                            Log.e("ERRORMAIN", "UNK");
                        }
                    }
                });
    }

    public static String getAuthorizationHeader() {
        String credential =  "client:secret";
        return "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
    }
}
