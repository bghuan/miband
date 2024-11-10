package com.xiaomi.xms.wearable.demo;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import com.xiaomi.xms.wearable.Status;
import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.AuthApi;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.demo.databinding.ActivityMainBinding;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener;
import com.xiaomi.xms.wearable.node.Node;
import com.xiaomi.xms.wearable.node.NodeApi;
import com.xiaomi.xms.wearable.notify.NotifyApi;
import com.xiaomi.xms.wearable.tasks.OnFailureListener;
import com.xiaomi.xms.wearable.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    static String band_id = "";
    static String server_ip = "192.168.10.100";
    static int server_port = 13000;
    static String server_message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> {
            band_connnet();
        });
        binding.fab2.setOnClickListener(view -> {
            NodeApi api = Wearable.getNodeApi(MainActivity.this);
            api.launchWearApp(band_id,"index").addOnSuccessListener(var1 -> {
            }).addOnFailureListener(var1 -> {
            });
        });
        binding.fab3.setOnClickListener(view -> {
            band_notify();
        });
    }

    private class ConnectSocketTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                InetAddress serverAddress = InetAddress.getByName(server_ip);
                Socket socket = new Socket(serverAddress, server_port);
                OutputStream outputStream = socket.getOutputStream();
                server_message += "," + System.currentTimeMillis();
                outputStream.write(server_message.getBytes());
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                alert(e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    private void alert(String str) {
        Snackbar.make(binding.getRoot(), str, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show();
    }
    private void alert2(String str) {
        Snackbar.make(binding.getRoot(), str, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show();
    }

    private void band_notify() {
        if (band_id.isEmpty()) return;
        NotifyApi notifyApi = Wearable.getNotifyApi(MainActivity.this);
        notifyApi.sendNotify(band_id, "biu", "biubiubiu")
                .addOnSuccessListener(status -> {
                }).addOnFailureListener(e -> {
                    alert(e.getMessage());
                });
    }

    private void band_listen() {
        if (band_id.isEmpty()) return;
        OnMessageReceivedListener onMessageReceivedListener = (nodeId, message) -> {
            String str = new String(message);
            str += System.currentTimeMillis();
            server_message = str;
            new ConnectSocketTask().execute();
            alert2(str);
        };
        MessageApi messageApi = Wearable.getMessageApi(MainActivity.this);
        messageApi.addListener(band_id, onMessageReceivedListener)
                .addOnSuccessListener(var1 -> {
                }).addOnFailureListener(e -> {
                    alert(e.getMessage());
                });

    }

    private void band_connnet() {
        if (!band_id.isEmpty()) return;
        NodeApi api = Wearable.getNodeApi(MainActivity.this);
        api.getConnectedNodes().addOnSuccessListener(nodes -> {
            band_id = nodes.get(0).id;
            band_permission();
            band_listen();
        }).addOnFailureListener(e -> {
        });
    }

    private void band_permission() {
        AuthApi authApi = Wearable.getAuthApi(MainActivity.this);
        authApi.requestPermission(band_id, Permission.DEVICE_MANAGER, Permission.NOTIFY)
                .addOnSuccessListener(permissions -> {
                }).addOnFailureListener(e -> {
                    alert(e.getMessage());
                });
    }

}