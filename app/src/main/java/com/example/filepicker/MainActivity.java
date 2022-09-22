package com.example.filepicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sky.filepicker.upload.Constants;
import com.sky.filepicker.upload.LocalUpdateActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_main_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
            }
        });
    }

    public void open() {
        RxPermissionHelper helper = new RxPermissionHelper(this);
        helper.requestEach(new RxPermissionHelper.PermissionCallback() {
            @Override
            public void granted(String permissionName) {
                Intent intent = new Intent(MainActivity.this, LocalUpdateActivity.class);
                intent.putExtra("maxNum", Integer.MAX_VALUE);//设置最大选择数
                startActivityForResult(intent, Constants.UPLOAD_FILE_REQUEST);
            }

            @Override
            public void denied(String permissionName, boolean forever) {

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.UPLOAD_FILE_REQUEST && resultCode == Constants.UPLOAD_FILE_RESULT) {
            List<String> list = data.getStringArrayListExtra("pathList");//这里的list就是选择的文件列表的集合
            StringBuilder totalPath = new StringBuilder();

            for (String s : list) {
                Log.d(TAG, "onActivityResult: " + s);
                totalPath.append(s);
                File file1 = new File(s);
//                file1.getTotalSpace();
                Toast.makeText(MainActivity.this, s + file1.getTotalSpace(), Toast.LENGTH_SHORT).show();
                try {
                    String temFile = readExternal(MainActivity.this, s);
                    Log.d(TAG, "readFileResult: " + temFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(MainActivity.this, totalPath.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从sd card文件中读取数据
     * @param filename 待读取的sd card
     * @return
     * @throws IOException
     */
    public static String readExternal(Context context, String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//            filename = context.getExternalCacheDir().getAbsolutePath() + File.separator + filename;
            //打开文件输入流
            FileInputStream inputStream = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while(len > 0){
                sb.append(new String(buffer,0,len));

                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        }
        return sb.toString();
    }
}