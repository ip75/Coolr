package ru.cocsosoft;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImagePreviewAll extends Fragment  implements OnClickListener {
	private ImageButton nextButton;
	private ImageButton back_to_photo2_button;

	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		//View v = inflater.inflate(R.layout.fragment_preview_one, parent, false);
		View v = inflater.inflate(R.layout.fragment_preview_all, null);
		ImageView imgView1 = (ImageView) v.findViewById(R.id.box_preview_image1);
		ImageView imgView2 = (ImageView) v.findViewById(R.id.box_preview_image2);
		ThatBetterActivity act = (ThatBetterActivity) getActivity();
		
		/*
		 * показываем превью из сохраненого файла
		//покажем сохр. файлы для превью
		LocalFile lf = new LocalFile();					
												
		String fileName1 = act.getImageFileName(1);
		String fileName2 = act.getImageFileName(2);
		String sDirPath1 = lf.appDirToFileName(fileName1);
		String sDirPath2 = lf.appDirToFileName(fileName2);
		
		imgView1.setImageDrawable(Drawable.createFromPath(sDirPath1+"/"+fileName1));
		imgView2.setImageDrawable(Drawable.createFromPath(sDirPath2+"/"+fileName2));
		*/
		
		byte[] scaledData1 = act.getBytePhoto(1);
		Bitmap bmp = BitmapFactory.decodeByteArray(scaledData1, 0, scaledData1.length);
		imgView1.setImageBitmap(bmp);
		//bmp.recycle(); //убиваем
		//bmp = null;
		
		byte[] scaledData2 = act.getBytePhoto(2);
		Bitmap bmp2 = BitmapFactory.decodeByteArray(scaledData2, 0, scaledData2.length);
		imgView2.setImageBitmap(bmp2);
		//bmp2.recycle(); //убиваем
		//bmp2 = null;
		
		
		nextButton = (ImageButton) v.findViewById(R.id.next_button);
		back_to_photo2_button = (ImageButton) v.findViewById(R.id.back_to_photo2_button);
		nextButton.setOnClickListener(this);
		back_to_photo2_button.setOnClickListener(this);
		
		
		

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
	     case R.id.back_to_photo2_button:
	       // фото не понравилось возвращаемся
	       // дописать удаление уже сохраненного !!!	 	    	 
	    	 act.setImageNum(currentImgNum-1);
	    	 act.openCameraView();
	       break;	   
	     case R.id.next_button:
	    	 //отправка
	    	 act.iWantSendBox();	    	 
	     }
	}
}
