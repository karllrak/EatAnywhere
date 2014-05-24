package com.example.eatanywhere;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {

	public static boolean loadImageFromPath(ImageView imgView, String fullImagePath ) {
		if ( null == imgView ) {
			return false;
		}
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(fullImagePath);
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inScaled = true;
			opt.inTargetDensity = opt.inScreenDensity / 30;
			Bitmap bmp = BitmapFactory.decodeStream(fin, null, opt);
			imgView.setImageBitmap(bmp);
			imgView.setAdjustViewBounds(true);
			imgView.setMaxHeight(300);
			imgView.setMaxWidth(150);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
	}

}