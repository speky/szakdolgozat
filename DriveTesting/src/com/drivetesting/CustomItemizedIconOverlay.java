package com.drivetesting;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;

public class CustomItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem>{
	private static ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();
	private Context context;

	public CustomItemizedIconOverlay(
			Context context, OnItemGestureListener<OverlayItem> onItemGestureListener, ResourceProxy resourceProxy) {
		super(overlayItemArray, onItemGestureListener, resourceProxy);
		this.context = context;
	}

	public void setLocation(GeoPoint location){
		overlayItemArray.clear();		     
		OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", location);
		overlayItemArray.add(newMyLocationItem);

	}

	@Override
	public void draw(Canvas canvas, MapView mapview, boolean arg) {
		super.draw(canvas, mapview, arg);
		if(!overlayItemArray.isEmpty()){

			//overlayItemArray have only ONE element only, so I hard code to get(0)
			GeoPoint in = overlayItemArray.get(0).getPoint();

			Point out = new Point();
			mapview.getProjection().toPixels(in, out);

			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_node);
			//shift the bitmap center
			canvas.drawBitmap(bm, 
					out.x - bm.getWidth()/2,  
					out.y - bm.getHeight()/2,  
					null);
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
		//return super.onSingleTapUp(event, mapView);
		return true;
	}
}
