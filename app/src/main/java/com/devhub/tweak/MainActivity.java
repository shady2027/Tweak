package com.devhub.tweak;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devhub.tweak.databinding.ActivityMainBinding;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //creating an instance of the binding class
    // used to access different views/elements using their ids present inside the layout file
    ActivityMainBinding binding;
    int IMAGE_REQUEST_CODE = 45;
    int CAMERA_REQUEST_CODE = 14;
    int RESULT_CODE = 200;

    private EditText search_et;
    private ImageView search_iv;
    private RecyclerView btn_rv, image_rv;
    private ProgressBar load_pb;
    private ArrayList<String> imageArrayList;
    private ArrayList<CategoryRVModal> categoryRVModalArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private ImageRVAdapter imageRVAdapter;
    private ImageView paint_btn;

    //to get the permission to save(write) the image in the external storage or app directory
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        search_et = findViewById(R.id.et_search);
        search_iv = findViewById(R.id.search_IV);
        btn_rv = findViewById(R.id.btn_container);
        image_rv = findViewById(R.id.image_container);
        load_pb = findViewById(R.id.pb_load);
        imageArrayList = new ArrayList<>();
        categoryRVModalArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this,RecyclerView.HORIZONTAL,false);
        btn_rv.setLayoutManager(linearLayoutManager);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModalArrayList,this,this::onCategoryClick);
        btn_rv.setAdapter(categoryRVAdapter);

        paint_btn = findViewById(R.id.paint_btn);

        paint_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),PaintActivity.class);
                startActivity(in);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        image_rv.setLayoutManager(gridLayoutManager);
        imageRVAdapter = new ImageRVAdapter(imageArrayList,this);
        image_rv.setAdapter(imageRVAdapter);

        //to get different genres of images
        getCategories();

        //to get images on the screen
        getWallpapers();

        search_iv.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             //to get the search text form the searchview
                                             String searchStr = search_et.getText().toString();
                                             if (searchStr.isEmpty()) {
                                                 Toast.makeText(MainActivity.this, "Please enter your search", Toast.LENGTH_SHORT).show();
                                             } else {
                                                 getWallpapersByCategory(searchStr);
                                             }
                                         }
        });


        // to implement the editing features
        //set on clickListener on the gallery icon of the menu item of bottomNavigationBar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                //when home icon is clicked
                case R.id.home:
                    Intent i = new Intent(this,MainActivity.class);
                    startActivity(i);
                    break;
                //when camera icon is clicked
                case R.id.camera:
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.CAMERA},32);
                    }else{
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
                    }
                    break;
                //when gallery icon is clicked
                case R.id.gallery:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // image/* used to get all the files that are an image type
                    intent.setType("image/*");
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                    break;
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // once the activity has started for the gallery function i.e. startActivityForResult its result ill be inside onActivityResult
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == IMAGE_REQUEST_CODE) {
                //when requestcode = 45, it means the photo is still unedited
                if (data.getData() != null) {
                    Uri filePath = data.getData();
                    Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
                    dsPhotoEditorIntent.setData(filePath);
                    dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Tweak");
                    dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#002D40"));
                    dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#000000"));
//                    int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
//                    dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
                    startActivityForResult(dsPhotoEditorIntent, RESULT_CODE);
                }else{
                    Intent i = new Intent(this,MainActivity.class);
                    startActivity(i);
                }
            }
            //if the requestcode = 200, it means the photo has been edited
            // this image now needs to be shown on the result screen
            if (requestCode == RESULT_CODE) {
                Intent intent = new Intent(this, ResultActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
            }

            if(requestCode == CAMERA_REQUEST_CODE) {
                if (checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                        Uri uri = getImageUri(photo);
                        Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
                        dsPhotoEditorIntent.setData(uri);
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Tweak");
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#002D40"));
                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#000000"));
//                        int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
//                        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
                        startActivityForResult(dsPhotoEditorIntent, RESULT_CODE);

                }
            }
        }

        public Uri getImageUri(Bitmap bitmap){
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,arrayOutputStream);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"Title",null);
            return Uri.parse(path);
        }

    private void getWallpapersByCategory(String category){
        imageArrayList.clear();
        load_pb.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/search?query="+category+"&per_page=30&page=1";
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray photoArray = null;
                load_pb.setVisibility(View.GONE);
                try {
                    photoArray = response.getJSONArray("photos");
                    for(int i =0;i< photoArray.length();i++){
                        JSONObject photoObj = photoArray.getJSONObject(i);
                        String imgUrl = photoObj.getJSONObject("src").getString("portrait");
                        imageArrayList.add(imgUrl);
                    }
                    imageRVAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Authorization","563492ad6f917000010000010da7422b00044ded9e803acf823965d4");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }


    private void getCategories() {
        categoryRVModalArrayList.add(new CategoryRVModal("Nature","https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Travel","https://images.pexels.com/photos/507410/pexels-photo-507410.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Arts","https://images.pexels.com/photos/3777876/pexels-photo-3777876.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Music","https://images.pexels.com/photos/1105666/pexels-photo-1105666.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Architecture","https://images.pexels.com/photos/776336/pexels-photo-776336.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Technology","https://images.pexels.com/photos/2582937/pexels-photo-2582937.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Abstract","https://images.pexels.com/photos/2693208/pexels-photo-2693208.png?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVModalArrayList.add(new CategoryRVModal("Cars","https://images.pexels.com/photos/337909/pexels-photo-337909.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"));
        categoryRVAdapter.notifyDataSetChanged();

    }

    private void getWallpapers() {
        imageArrayList.clear();
        load_pb.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/curated?per_page=30&page=1";
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                load_pb.setVisibility(View.GONE);
                try {
                    JSONArray photoArray = response.getJSONArray("photos");
                    for(int i =0;i< photoArray.length();i++){
                        JSONObject photoObj = photoArray.getJSONObject(i);
                        String imgUrl = photoObj.getJSONObject("src").getString("portrait");
                        imageArrayList.add(imgUrl);
                    }
                    imageRVAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Authorization","563492ad6f917000010000010da7422b00044ded9e803acf823965d4");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void onCategoryClick(int position) {
        String category = categoryRVModalArrayList.get(position).getCategory();
        getWallpapersByCategory(category);
    }


    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

        public void showDialog(final String msg, final Context context, final String permission) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Permission necessary");
            alertBuilder.setMessage(msg + " permission is necessary");
            alertBuilder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[] { permission }, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // can perform some task, but don't want to
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}