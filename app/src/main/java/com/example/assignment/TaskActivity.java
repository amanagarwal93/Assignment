package com.example.assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.example.assignment.databinding.ActivityTaskBinding;
import com.example.assignment.login.LoginActivity;
import com.example.assignment.utility.DataStorage;
import com.example.assignment.utility.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TaskActivity extends AppCompatActivity {

    private String TAG = TaskActivity.class.getSimpleName();

    private ActivityTaskBinding binding;
    private int PICK_IMAGE_MULTIPLE = 1;
    private String imageEncoded;
    private ArrayList<String> imagesEncodedList;

    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int REQUEST_PERMISSION_SETTING_STORAGE = 102;

    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] storagePermissionsRequired = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isFileExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task);

        binding.loginOptionsBtn.setOnClickListener(v -> openActivity(LoginActivity.class));

        binding.trackLocationBtn.setOnClickListener(v -> checkPermissions("maps"));

        binding.downloadFilesBtn.setOnClickListener(v -> checkPermissions("files"));

        binding.imageVideoBtn.setOnClickListener(v -> checkPermissions("imageVideo"));
    }

    /**
     * Navigate activity
     */

    private void openActivity(Class<? extends Activity> activity) {
        startActivity(new Intent(TaskActivity.this, activity));
    }

    /**
     * Download dialog
     */

    private void showDownloadDialog() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View view = LayoutInflater.from(this).inflate(R.layout.downlaod_dialog, viewGroup, false);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setView(view);

        AlertDialog alertDialog = materialAlertDialogBuilder.create();
        AppCompatEditText url = view.findViewById(R.id.url);
        view.findViewById(R.id.downloadBtn).setOnClickListener(v -> {
            String downloadURL = url.getText().toString();
            if (downloadURL.isEmpty()) {
                url.setError("Please enter a url");
                url.setFocusable(true);
            } else if (!URLUtil.isValidUrl(downloadURL)) {
                url.setError("Please enter a valid url");
                url.setFocusable(true);
            } else {
                if (checkUrl(downloadURL)) {
                    try {
                        isFileExist = new Task().execute(downloadURL).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isFileExist) {
                        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        Uri uri = Uri.parse("https://drive.google.com/file/d/1P5iJ929Yn-9zWH7GbGqRov_nzYBke6jW/view?usp=sharing");

                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setTitle("My File");
                        request.setDescription("Downloading");
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setVisibleInDownloadsUi(false);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "game-of-life");
                        downloadmanager.enqueue(request);
                        Utils.showToastMessage(binding.getRoot(), "Downloaded successfully");
                    } else {
                        url.setError("File does not exist");
                        url.setFocusable(true);
                    }
                } else {
                    url.setError("Please enter a valid url");
                    url.setFocusable(true);
                }
            }
        });

        view.findViewById(R.id.cancelBtn).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    /**
     * check file exist or not for a specified URL
     */

    @SuppressLint("StaticFieldLeak")
    private class Task extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                urlConnection.setRequestMethod("HEAD");
                return (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: " + result);
        }
    }

    /**
     * @param url
     * @return URL validations
     */

    private boolean checkUrl(String url) {
        boolean isURL = Patterns.WEB_URL.matcher(url).matches();
        if (isURL) {
            if (URLUtil.isNetworkUrl(url)) {
                try {
                    new URL(url);
                    isURL = true;
                    Log.d(TAG, "checkUrl: valid url");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, "checkUrl: invalid url ");
        }
        return isURL;
    }

    /**
     * Image/Video Dialog
     */

    private void showImageVideoDialog() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View view = LayoutInflater.from(this).inflate(R.layout.image_video_dialog, viewGroup, false);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setView(view);

        AlertDialog alertDialog = materialAlertDialogBuilder.create();
        view.findViewById(R.id.imageBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, AlbumSelectActivity.class);
            //set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 5);
            startActivityForResult(intent, Constants.REQUEST_CODE);
            alertDialog.dismiss();
        });

        view.findViewById(R.id.videoBtn).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Video"),7);
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    /**
     * check run time permissions
     */
    private void checkPermissions(String type) {
        if (Utils.getInstance().isNetworkAvailable(this)) {

            if (type.equals("maps")) {
                if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {
                    openActivity(MapsActivity.class);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    DataStorage.getInstance(this).setName("maps");
                    requestPermissions(permissionsRequired, REQUEST_PERMISSION_SETTING);
                } else {
                    Utils.showShortToastMessage(binding.rootLayout, "No Internet Connection");
                }
            } else if (type.equals("files")) {
                if (ActivityCompat.checkSelfPermission(this, storagePermissionsRequired[0]) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, storagePermissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {
                    showDownloadDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    DataStorage.getInstance(this).setName("files");
                    requestPermissions(storagePermissionsRequired, REQUEST_PERMISSION_SETTING);
                } else {
                    Utils.showShortToastMessage(binding.rootLayout, "No Internet Connection");
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, storagePermissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                    showImageVideoDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    DataStorage.getInstance(this).setName("imageVideo");
                    requestPermissions(storagePermissionsRequired, REQUEST_PERMISSION_SETTING);
                } else {
                    Utils.showShortToastMessage(binding.rootLayout, "No Internet Connection");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {

            if (permissions.length == 0) {
                return;
            }
            boolean allPermissionsGranted = true;
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
            }
            if (!allPermissionsGranted) {
                boolean somePermissionsForeverDenied = false;
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        //denied
                        Log.d("denied", permission);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(permissionsRequired, REQUEST_PERMISSION_SETTING);
                        }
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            //allowed
                            Log.d("allowed", permission);
                        } else {
                            //set to never ask again
                            Log.d("set to never ask again", permission);
                            somePermissionsForeverDenied = true;
                        }
                    }
                }
                if (somePermissionsForeverDenied) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Permissions Required")
                            .setMessage("You have forcefully denied some of the required permissions " +
                                    "for this action. Please open settings, go to permissions and allow them.")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
            } else {
                Log.d(TAG, "permissions granted");
                if (DataStorage.getInstance(this).getName().equals("maps")) {
                    openActivity(MapsActivity.class);
                } else if (DataStorage.getInstance(this).getName().equals("files")) {
                    showDownloadDialog();
                } else {
                    showImageVideoDialog();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {
            case 7:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult: " + data.getData());
                    startActivity(new Intent(TaskActivity.this, MediaActivity.class)
                            .putExtra("data", data.getData().toString())
                            .putExtra("type", "video"));
                }
                break;
            case 2000:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> strings = new ArrayList<>();
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0, l = images.size(); i < l; i++) {
                        strings.add(images.get(i).path);
                        Log.d(TAG, "onActivityResult: " + images.get(i).path);
                        stringBuffer.append(images.get(i).path + "\n");
                    }
                    startActivity(new Intent(TaskActivity.this,MediaActivity.class)
                            .putStringArrayListExtra("images",strings).putExtra("type","image"));
                }

                /*try {
                    // When an Image is picked
                    if (resultCode == RESULT_OK && null != data) {

                        // Get the Image from data
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        imagesEncodedList = new ArrayList<String>();
                        if (data.getData() != null) {

                            Uri mImageUri = data.getData();

                            // Get the cursor
                            Cursor cursor = getContentResolver().query(mImageUri,
                                    filePathColumn, null, null, null);
                            // Move to first row
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                cursor.close();
                                Log.d(TAG, "onActivityResult: getData " + imageEncoded);
                            }
                        } else {
                            if (data.getClipData() != null) {
                                ClipData mClipData = data.getClipData();
                                ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                                for (int i = 0; i < mClipData.getItemCount(); i++) {

                                    ClipData.Item item = mClipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    Log.d(TAG, "onActivityResult: data" +item.getUri());
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                                    mArrayUri.add(uri);
                                    // Get the cursor
                                    Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                    // Move to first row
                                    if (cursor != null) {
                                        cursor.moveToFirst();
                                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                        imageEncoded = cursor.getString(columnIndex);
                                        Log.d(TAG, "onActivityResult: " + imageEncoded);
                                        imagesEncodedList.add(imageEncoded);
                                        cursor.close();
                                    }
                                    *//*if (mArrayUri.size() > 5) {
                                        Utils.showShortToastMessage(binding.getRoot(), "Maximum limit reached");
                                    }*//*
                                }
                                Log.d(TAG, "Selected Images" + mArrayUri.size());
*/
//                                Bundle bundle = new Bundle();
//                                bundle.putSerializable("list",new Gson().toJson(mArrayUri));
//                                startActivity(new Intent(TaskActivity.this,MediaActivity.class)
//                                        .putExtra("images",bundle).putExtra("type","image"));
//                            }
//                        }
//                    } else {
//                        Utils.showShortToastMessage(binding.getRoot(), "You haven't picked Image");
//                    }
//                } catch (Exception e) {
//                    Utils.showShortToastMessage(binding.getRoot(), "Something went wrong");
//                }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}