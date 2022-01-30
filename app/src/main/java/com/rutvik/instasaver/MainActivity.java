package com.rutvik.instasaver;


import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    EditText link;
    Button download;
    TextView downloadStatus;
    ImageView instaImg;

    LinearLayout downloadPage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_foreground);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        link = findViewById(R.id.link);
        download = findViewById(R.id.download);
        downloadStatus = findViewById(R.id.downloadStatus);
        instaImg = findViewById(R.id.instaImg);
        downloadPage = findViewById(R.id.downloadPage);

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String instaLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (isStoragePermissionGranted()) {
                if (!isInternetAvailable()) {
                    Toast.makeText(this, "No internet!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (instaLink != null
                            && (instaLink.contains("?utm_source=ig_web_copy_link") || instaLink.contains("?utm_source=ig_web_button_share_sheet")
                            || instaLink.contains("?utm_source=ig_web_button_share_sheet") || instaLink.contains("?utm_medium=share_sheet") ||
                            instaLink.contains("?utm_medium=copy_link"))) {
                        // We have a valid link
                        downloadPage.setVisibility(View.INVISIBLE);
                        InstaVideo.downloadVideo(MainActivity.this, instaLink,instaImg,downloadStatus,true,dialog);

                        registerReceiver(onComplete,
                                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    } else {
                        finish();
                        Toast.makeText(this, "This is not a insta link!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    if (!isInternetAvailable()) {
                        showInternetDialog();
                    } else {
                        String instaLink = link.getText().toString();
                        if (instaLink != null
                                && (instaLink.contains("?utm_source=ig_web_copy_link") || instaLink.contains("?utm_source=ig_web_button_share_sheet")
                                || instaLink.contains("?utm_source=ig_web_button_share_sheet") || instaLink.contains("?utm_medium=share_sheet") ||
                                instaLink.contains("?utm_medium=copy_link"))) {
                            InstaVideo.downloadVideo(MainActivity.this, instaLink, instaImg,downloadStatus,false, dialog);

                            registerReceiver(onComplete,
                                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        } else {
                            Toast.makeText(MainActivity.this, "This is not a insta link!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void showInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet !")
                .setMessage("this app need internet for downloading.")
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "Download Complete", Toast.LENGTH_LONG).show();
            downloadStatus.setText("Download Complete");
            if (dialog.isShowing()){
                dialog.dismiss();
            }

            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Download Complete");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogg, int which) {
                    dialogg.dismiss();
                }
            });
            builder.create().show();
        }
    };

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Click download again!", Toast.LENGTH_LONG).show();
            //resume tasks needing this permission
        } else {
            Toast.makeText(getApplicationContext(), "Storage need!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.github:
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse("https://github.com/rutvikgondaliya"));
                startActivity(intent1);
                break;
            case R.id.insta:
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse("https://www.instagram.com/rutvik._.gondaliya/"));
                startActivity(intent2);
                break;
            case R.id.share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Insta Saver");
                i.putExtra(Intent.EXTRA_TEXT, "https://github.com/rutvikgondaliya");
                startActivity(Intent.createChooser(i, "Share InstaSaver"));
                break;
            case R.id.moreapp:
                Intent intent3 = new Intent(Intent.ACTION_VIEW);
                intent3.setData(Uri.parse("https://www.linkedin.com/in/rutvikkumar-gondaliya-8670741b6/"));
                startActivity(intent3);
                break;
            case R.id.exit:
                finish();
                break;
        }

        return true;
    }
}