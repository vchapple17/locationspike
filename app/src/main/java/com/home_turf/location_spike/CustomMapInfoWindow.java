package com.home_turf.location_spike;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.InputStream;


// Resource: http://www.zoftino.com/google-maps-android-custom-info-window-example
public class CustomMapInfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomMapInfoWindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }


    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.map_custom_infowindow, null);

        TextView name = view.findViewById(R.id.name);
        TextView description = view.findViewById(R.id.description);
        ImageView img = view.findViewById(R.id.img);

        name.setText(marker.getTitle());
        description.setText(marker.getSnippet());

        CustomMapInfoWindowData infoWindowData = (CustomMapInfoWindowData) marker.getTag();

//        int imageId = context.getResources().getIdentifier(infoWindowData.getImgName().toLowerCase(),
//                "drawable", context.getPackageName());
//        img.setImageResource(imageId);
        img.setImageBitmap(getBitmapFromAssets(infoWindowData.getImgName()));

        name.setText(infoWindowData.getName());
        description.setText(infoWindowData.getDescription());

        return view;
    }

    private Bitmap getBitmapFromAssets(String filename) {
        AssetManager am = context.getAssets();
        InputStream is = null;

        try {
            is = am.open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(is);
    }

}
