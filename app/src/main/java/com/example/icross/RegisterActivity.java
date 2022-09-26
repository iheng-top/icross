package com.example.icross;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Typeface typeface;
    RecyclerView recyclerView;
    TextView registerHeader;
    TextView txtName;
    TextView txtLocation;
    EditText registerName;
    EditText registerLocation;
    Button btnGet;
    Button btnSubmit;
    Button btnStart;
    private static final String TAG = "RegisterActivity";
    public LocationClient mLocationClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).hide();

        registerHeader = (TextView) findViewById(R.id.register_header);
        txtName = (TextView) findViewById(R.id.register_name_label);
        txtLocation = (TextView) findViewById(R.id.register_location_label);
        registerName = (EditText) findViewById(R.id.register_name);
        registerLocation = (EditText) findViewById(R.id.register_location);
        btnGet = (Button) findViewById(R.id.btn_get);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnSubmit = (Button) findViewById(R.id.btn_register);
        recyclerView = (RecyclerView) findViewById(R.id.rank_list);

        typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
        registerHeader.setTypeface(typeface);
        txtName.setTypeface(typeface);
        txtLocation.setTypeface(typeface);
        btnGet.setTypeface(typeface);
        btnSubmit.setTypeface(typeface);
        btnStart.setTypeface(typeface);

        btnGet.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnStart.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get:
                getCity();
                break;
            case R.id.btn_register:
                try {
                    register();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_start:
                finish();
                break;
            default:
                break;
        }
    }

    public void register() throws JSONException {
        getPermission();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = Common.baseUrl + "/register";
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        String name = registerName.getText().toString();
        String location = registerLocation.getText().toString();
        if ("".equals(name) || "".equals(location)) {
            Toast.makeText(RegisterActivity.this, "请将信息完善后再进行操作！", Toast.LENGTH_SHORT).show();
            return;
        }
        jsonObject.put("name", name);
        jsonObject.put("location", location);
        String json = jsonObject.toString();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        // 异步请求
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showResponse(0, e.getMessage());
                // Log.d(TAG, "onFailure: response: " + message.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                showResponse(1, response.body().string());
                // Log.d(TAG, "onResponse: response: " + message.obj.toString());
            }
        });
    }

    private void showResponse(int what, String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (what) {
                    case 0:
                        Toast.makeText(RegisterActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        String res = response;
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            int code = jsonObject.getInt("code");
                            if (code == 20000) {
                                Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Common.userName = registerName.getText().toString();
                        Common.location = registerLocation.getText().toString();
                        MainActivity.txtName.setText(Common.userName);
                        Properties properties = new Properties();
                        try {
                            // 保存用户名
                            FileOutputStream fos = new FileOutputStream(new File(getCacheDir(), "info"));
                            properties.setProperty("name", Common.userName);
                            properties.setProperty("location", Common.location);
                            properties.store(fos, null);
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    private void getCity() {
        getPermission();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                String city = bdLocation.getCity();    //获取城市
                String coorType = bdLocation.getCoorType();
                int errorCode = bdLocation.getLocType();
                // Toast.makeText(RegisterActivity.this, String.valueOf(errorCode), Toast.LENGTH_SHORT).show();
                registerLocation.setText(city);
                if (errorCode != 161) {
                    Toast.makeText(RegisterActivity.this, String.valueOf(errorCode) + " 定位错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LocationClientOption option = new LocationClientOption();
        //可选，是否需要地址信息，默认为不需要，即参数为false
        option.setIsNeedAddress(true);
        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        option.setNeedNewVersionRgc(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void getPermission() {
        // 获取运行时权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
        }
    }
}