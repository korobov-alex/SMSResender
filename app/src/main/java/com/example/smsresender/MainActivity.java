package com.example.smsresender;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.textView);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);

        // Создаем новый объект Handler
        handler = new Handler();

        // Создаем новый объект Runnable, который будет выполняться каждую секунду
        runnable = new Runnable() {
            @Override
            public void run() {
                // Считываем SMS
                Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null,null,null);
                cursor.moveToFirst();

                // Устанавливаем текст TextView
                myTextView.setText(cursor.getString(12));

                // Запускаем Runnable снова через 1 секунду
                handler.postDelayed(runnable, 1000);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Запускаем Runnable
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Останавливаем Runnable
        handler.removeCallbacks(runnable);
        finish();
    }



}