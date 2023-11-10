package com.example.smsresender;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private Runnable runnable;
    public Button readSms;
    public Button okBtn;
    public EditText targetMailField;
    public TextView workingAnnounce;

    TextView textView;
    String GMAIL_USER = "alexkorobov95@gmail.com";
    String GMAIL_PASSWORD = "";
    private String targetMail = "alexkorobov95@gmail.com";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    textView = findViewById(R.id.textView);
    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
    startService(new Intent(this, SmsService.class));
    readSms = findViewById(R.id.read_sms);
    okBtn = findViewById(R.id.ok);
    targetMailField = findViewById(R.id.target_email);
    textView = findViewById(R.id.textView);
    workingAnnounce = findViewById(R.id.working);

    okBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            targetMail = targetMailField.getText().toString(); // Присваиваем значение targetEmail переменной targetMail
            textView.setText("You'll send the data to: " + targetMail);
        }
    });

    readSms.setOnClickListener((new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            workingAnnounce.setText("Program is working!");
        }
    }));

    // Создаем новый объект Handler
    handler = new Handler();

    // Создаем новый объект Runnable, который будет выполняться каждую секунду
    runnable = new Runnable() {
        private String previousValue;
        @Override
        public void run() {
            // Считываем SMS
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null,null,null);
            assert cursor != null;
            cursor.moveToFirst();

            // Получаем текущее значение
            String currentValue = cursor.getString(12);

            if (!Objects.equals(currentValue, previousValue)) {
                String sender = cursor.getString(2);
                // Если значение изменилось, вызываем функцию sendEmail
                sendEmail(currentValue, targetMail, sender);

                // Сохраняем текущее значение как предыдущее
                previousValue = currentValue;
            }
            // Устанавливаем текст TextView


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

    public void sendEmail(String smsBody, String receiverEmail, String sender) {

        try {
            String stringSenderEmail = GMAIL_USER;
            String stringPasswordSenderEmail = GMAIL_PASSWORD;

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            // Create a new MimeMessage
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));

            mimeMessage.setSubject("New SMS on TL's phone received!");
            mimeMessage.setText("Sender: " + sender + "\nText: " + smsBody);

            // Send the email
            Thread thread = new Thread(() -> {
                try {
                    Transport.send(mimeMessage);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
