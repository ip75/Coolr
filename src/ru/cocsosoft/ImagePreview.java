package ru.cocsosoft;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImagePreview extends Fragment implements OnClickListener {
	private ImageButton nextButton;
	private ImageButton back_to_photo1_button;

	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		//View v = inflater.inflate(R.layout.fragment_preview_one, parent, false);
		View v = inflater.inflate(R.layout.fragment_preview_one, null);


		nextButton = (ImageButton) v.findViewById(R.id.next_button);
		back_to_photo1_button = (ImageButton) v.findViewById(R.id.back_to_photo1_button);
		
		ImageView imgView = (ImageView) v.findViewById(R.id.box_preview_image);		
		ThatBetterActivity act = (ThatBetterActivity) getActivity();									
		int currentImgNum = act.getImageNum();
		/*
		 * показываем превью из сохраненого файла
		String fileName = act.getImageFileName(currentImgNum);
		LocalFile lf = new LocalFile();
		String fullPath = lf.appDirToFileName(fileName)+"/"+fileName;		
		imgView.setImageDrawable(Drawable.createFromPath(fullPath));
		*/
		byte[] scaledData = act.getBytePhoto(currentImgNum);
		Bitmap bmp = BitmapFactory.decodeByteArray(scaledData, 0, scaledData.length);
		imgView.setImageBitmap(bmp);
		//bmp.recycle(); //убиваем
		
		nextButton.setOnClickListener(this);
		back_to_photo1_button.setOnClickListener(this);
		
		return v;
	}
	

	@Override
	public void onClick(View arg0) 
	{
		ThatBetterActivity act = (ThatBetterActivity) getActivity();		
		int currentImgNum = act.getImageNum();
		// по id определеяем кнопку, вызвавшую этот обработчик
	     switch (arg0.getId()) 
	     {
	     case R.id.back_to_photo1_button:
	       // фото не понравилось возвращаемся
	       // дописать удаление уже сохраненного !!!	 	    	 
	    	 act.setImageNum(currentImgNum-1);
	    	 act.openCameraView();
	       break;	   
	     case R.id.next_button:
	    	 act.openCameraView();
	    	/* 
	       // след. фото																 							
				if (currentImgNum==1) 
					//если окно открыли после первого фото переходи опять к камере
				{					
					act.openCameraView();
				}
				else
				{
					//показываем две фотки перед отправкой
					act.openPreviewView(true);
					 
				}
				  */				 
		   break; 
	     }
	     
	}
}
