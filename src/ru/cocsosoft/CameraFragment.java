package ru.cocsosoft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

public class CameraFragment extends Fragment implements OnClickListener {

		
	private static final String TAG = "DebugLogs"; // для тех. логов
	private static final boolean isDEBUG = false; // вкл. логов
	
	private Camera camera;
	private SurfaceView surfaceView;
	private ParseFile photoFile;
	private ImageButton photoButton;	
	private ImageButton selectImgButton;

	//реализовать авто фокус
	//http://habrahabr.ru/post/112272/
	
	public void InitCamera()
	{
		if (camera == null) {
			try {
				camera = Camera.open();
				if (isDEBUG) Log.d(TAG, "camera.open()");
				photoButton.setEnabled(true);
			} catch (Exception e) {
				Log.e(TAG, "No camera with exception: " + e.getMessage());
				photoButton.setEnabled(false);
				Toast.makeText(getActivity(), String.format(Locale.getDefault(), "%s%s", getString(R.string.no_camera), e.getMessage()),
						Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
				
		if (isDEBUG) Log.d(TAG, "CameraFragment onCreateView");
		//View v = inflater.inflate(R.layout.fragment_camera, parent, false);
		View v = inflater.inflate(R.layout.fragment_camera, null);

		 
		photoButton = (ImageButton) v.findViewById(R.id.camera_photo_button);
		selectImgButton = (ImageButton) v.findViewById(R.id.select_img_button);
		photoButton.setOnClickListener(this);
		selectImgButton.setOnClickListener(this);
		InitCamera();
				
		
		
		surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface_view);
		surfaceView.setOnClickListener(this);
		
		
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(new Callback() {

			public void surfaceCreated(SurfaceHolder holder) {
				try {
					if (camera != null) {
						// портретный вид, для ланшафта = 0
						camera.setDisplayOrientation(90);
						camera.setPreviewDisplay(holder);
						camera.startPreview();
						camera.autoFocus(null);
					}
				} catch (IOException e) {
					Log.e(TAG, "Error setting up preview", e);
				}
				
				setPreviewSize(false);
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				// nothing to do here
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				// nothing here
			}
						

		});

		return v;
	}

	
	public void cameraClick(){
		if (camera == null)
		{
			if (isDEBUG) Log.d(TAG, "camera == null");
			return;
		}
		
		if (isDEBUG) Log.d(TAG, "camera.takePicture");
		
		camera.takePicture(new Camera.ShutterCallback() 
		{

			@Override
			public void onShutter() 
			{
				// nothing to do
				//shutter — вызывается в момент получения изображения с матрицы	
			}

		}, null, new Camera.PictureCallback() 
		{

			@Override
			public void onPictureTaken(byte[] data, Camera camera) 
			{
				if (isDEBUG) Log.d(TAG, "saveScaledPhoto");	
				saveScaledPhoto(data);
				return ;
			}

		});

	}
	

	
	@Override
	public void onClick(View arg0) 
	{
		// по id определеяем кнопку, вызвавшую этот обработчик
	     switch (arg0.getId()) 
	     {
	     case R.id.camera_photo_button:
	       // делаем фото
	       cameraClick();
	       break;	   
	     case R.id.select_img_button:
	       // выбираем фото
	    	 ThatBetterActivity act = (ThatBetterActivity) getActivity();
	    	 act.imgSelect();	    	 
		   break; 
	     case R.id.camera_surface_view:
	    	 if (camera != null) {
				camera.autoFocus(null);
		        cameraClick();
				}
		       break;	   
	     }
	}

	
	/*
	 * implements OnClickListener, Camera.AutoFocusCallback
	 @Override
	    public void onAutoFocus(boolean paramBoolean, Camera paramCamera)
	    {
	        if (paramBoolean)
	        {
	            // если удалось сфокусироваться, делаем снимок
	            //paramCamera.takePicture(null, null, null, this);
	        	cameraClick();
	        }
	    }
	    */
	
	/*
	 * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
	 * they are saved. Since we never need a full-size image in our app, we'll
	 * save a scaled one right away.
	 */
	private void saveScaledPhoto(byte[] data) {

		if (camera != null) {
			camera.stopPreview();
			/* нужно ли тут камеру прибивать ???
			camera.release();
			if (isDEBUG) Log.d(TAG, "camera.release()");
			*/
		}
		
		ThatBetterActivity act = (ThatBetterActivity) getActivity();	
		
		act.processingImageDate(data,true);							
		act.openPreviewView();
	
		return ;
		
	}



	@Override
	public void onResume() {
		super.onResume();
		if (isDEBUG) Log.d(TAG, "CameraFragment onResume");		
		InitCamera();
		//setPreviewSize(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (isDEBUG) Log.d(TAG, "CameraFragment onPause");
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
			if (isDEBUG) Log.d(TAG, "camera.release()");
		}		
	}
	

	 
	  void setPreviewSize(boolean fullScreen) {
		  
		  /*
		   * http://habrahabr.ru/post/112272/undefined/
		   
		  Size previewSize = camera.getParameters().getPreviewSize();
	        float aspect = (float) previewSize.width / previewSize.height;

	        int previewSurfaceWidth = surfaceView.getWidth();
	        int previewSurfaceHeight = surfaceView.getHeight();

	        //LayoutParams lp = surfaceView.getLayoutParams();
	        
	        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

	        // здесь корректируем размер отображаемого preview, чтобы не было искажений

	        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
	        {
	            // портретный вид
	            camera.setDisplayOrientation(90);
	            lp.height = previewSurfaceHeight;
	            lp.width = (int) (previewSurfaceHeight / aspect);
	            ;
	        }
	        else
	        {
	            // ландшафтный
	            camera.setDisplayOrientation(0);
	            lp.width = previewSurfaceWidth;
	            lp.height = (int) (previewSurfaceWidth / aspect);
	        }

	        surfaceView.setLayoutParams(lp);
	        http://habrahabr.ru/post/112272/undefined/
	        
	        */
		  
		  //!!! едет вся разметка !!!
            //Автор http://startandroid.ru/ru/uroki/vse-uroki-spiskom/264-urok-132-kamera-vyvod-izobrazhenija-na-ekran-obrabotka-povorota.html
		  
		    // получаем размеры экрана
		    //Display display = getWindowManager().getDefaultDisplay();
		    //boolean widthIsMax = display.getWidth() > display.getHeight();
		    
		    WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE); 
		    Display display = wm.getDefaultDisplay();  
		    boolean widthIsMax = display.getWidth() > display.getHeight();
		    

		    // определяем размеры превью камеры
		    Size size = camera.getParameters().getPreviewSize();
		        
		    RectF rectDisplay = new RectF();
		    RectF rectPreview = new RectF();
		    
		    // RectF экрана, соотвествует размерам экрана
		    rectDisplay.set(0, 0, display.getWidth(), display.getHeight());
		    
		    // RectF первью 
		    if (widthIsMax) {
		      // превью в горизонтальной ориентации
		      rectPreview.set(0, 0, size.width, size.height);
		    } else {
		      // превью в вертикальной ориентации
		      rectPreview.set(0, 0, size.height, size.width);
		    }

		    Matrix matrix = new Matrix();
		    // подготовка матрицы преобразования
		    if (!fullScreen) {
		      // если превью будет "втиснут" в экран (второй вариант из урока)
		      matrix.setRectToRect(rectPreview, rectDisplay,
		          Matrix.ScaleToFit.START);
		    } else {
		      // если экран будет "втиснут" в превью (третий вариант из урока)
		      matrix.setRectToRect(rectDisplay, rectPreview,
		          Matrix.ScaleToFit.START);
		      matrix.invert(matrix);
		    }
		    // преобразование
		    matrix.mapRect(rectPreview);

		    // установка размеров surface из получившегося преобразования
		    surfaceView.getLayoutParams().height = (int) (rectPreview.bottom);
		    surfaceView.getLayoutParams().width = (int) (rectPreview.right);
		    
		  }


	
}
