package com.tizianobasile.wearsdkdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * Created by tiziano on 22/10/14.
 */
public class DataItemSyncActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //REQUEST CODES
    public static final int GALLERY_INTENT = 1;

    //DATA LAYER
    private GoogleApiClient mGoogleApiClient;

    //UI
    private EditText mDataItemMessageEditText;
    private Button mSyncButton;
    private ImageView mAssetImageView;

    //UTILS
    private Bitmap mAssetBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_item_sync);
        //Register GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        //Get UI Elements references
        mDataItemMessageEditText = (EditText) findViewById(R.id.messageET);
        mSyncButton = (Button) findViewById(R.id.syncBtn);
        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dataItemMessage = mDataItemMessageEditText.getText().toString();
                PutDataMapRequest mDataMapRequest = PutDataMapRequest.create("/data");
                mDataMapRequest.getDataMap().putString("DATA_MESSAGE", dataItemMessage);
                if(mAssetBitmap != null){
                    Asset mAsset = createAssetFromBitmap(mAssetBitmap);
                    mDataMapRequest.getDataMap().putAsset("ASSET_BITMAP", mAsset);
                }
                PutDataRequest mDataRequest = mDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> mPendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, mDataRequest);
            }
        });
        mAssetImageView = (ImageView) findViewById(R.id.assetImageView);
        mAssetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Build.VERSION.SDK_INT < 19){
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);
                } else {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){
            final Uri mSelectedImageUri = data.getData();
            String mCurrentPhotoPath = getPath(mSelectedImageUri);
            mAssetBitmap = createBitmap(mAssetImageView, mCurrentPhotoPath);
            Log.d("MOBILE", mAssetBitmap.toString());
            mAssetImageView.setImageBitmap(mAssetBitmap);
            mAssetImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //INTERFACES IMPLEMENTATION

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("MOBILE", "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MOBILE", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("MOBILE", "GoogleApiClient connection failed");
    }


    //CUSTOM METHODS
    public String getPath(Uri uri) {
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        Cursor mCursor = null;
        try{
            String[] projection = {MediaStore.Images.Media.DATA };
            mCursor = getContentResolver().query(uri, projection, null, null, null);
            int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            mCursor.moveToFirst();
            String fileUri = mCursor.getString(column_index);
            return fileUri;
        }finally {
            if(mCursor != null){
                mCursor.close();
            }
        }
    }

    private Bitmap createBitmap(ImageView imageView, String imgPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOptions);
        return bitmap;
    }

    public Asset createAssetFromBitmap(Bitmap mBitmap){
        final ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mByteArrayOutputStream);
        return Asset.createFromBytes(mByteArrayOutputStream.toByteArray());
    }
}
