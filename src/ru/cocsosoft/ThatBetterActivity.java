package ru.cocsosoft;



import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


//public class ThatBetterActivity extends Activity  implements OnClickListener{
public class ThatBetterActivity extends Activity {

	public static final String TAG = "DebugLogs"; // для тех. логов
	private static final boolean isDEBUG = true; // вкл. логов
	private static final String fTAG_camera = "FRAGMENT_Camera";
	private static final String fTAG_preview = "FRAGMENT_Preview";
	private static final String fTAG_previewAll = "FRAGMENT_PreviewAll";
	private static final String fTAG_vote = "FRAGMENT_Vote";
	private static final String fTAG_profile = "FRAGMENT_Profile";
	
	
	private int iImageNum; //номер текущего фото в боксе
	private String sImageFileName1; //
	private String sImageFileName2; //
	private ParseFile pPhotoFile1;
	private ParseFile pPhotoFile2;
	private byte[] bytePhotoFile1;
	volatile boolean needSavePhotoFile1 = false;
	private byte[] bytePhotoFile2;
	volatile boolean needSavePhotoFile2 = false;

	private String currentBoxID;
    private String sLocalFileName1;
    private String sLocalFileName2;
    
	/*
	 Инициализация = 0
	 Сохранение фото значение +1
	 */ 
	

    CameraFragment fragmentCamera = null;
	ImagePreview fragmentPreview = null;
	VoteFragment fragmentVote = null;
	ProfileFragment fragmentProfile = null;
	FragmentManager fManager;
	//FrameLayout fContainer;
	
	//переменные для потоков
	//ProgressDialog progressDialog;
	BackgroundJob backgroundJob; 
	
	volatile boolean isWorkFillingCacheVoteList = false;
	volatile boolean isReadyForSendBox = false;
	
	//массив с ИД боксов которые мы просмотрели
	ArrayList<String> arrayExcludeVoteCache;
	
	public volatile ArrayList<CacheVoteBox> cacheVoteList;
	
	ThatBetter aplCtx;
	private Context mContext;
	
	//Переменные для удаления фоток из кеша при просмотре вотфрагмент
	private boolean bDeleteCacheFiles = false;
	private ArrayList<String> arrDeleteFiles = new ArrayList<String>();
	
	//пуш сообщение
	void notiMsqNewVote(String msqTitle,String msqText){
		if (TextUtils.equals("", msqText)) return;
			
		// покажем
		Intent intent = new Intent(mContext, mContext.getClass());
		intent.putExtra("menuFragment", "openProfile");

		
		//!!!  сделать слушателя и обработку в которой открываем - openProfile()
		//http://developer.alexanderklimov.ru/android/notification.php
		PendingIntent pIntent = PendingIntent.getActivity(mContext, 1,
				intent, 0);
	

		
		Notification noti = new Notification.Builder(mContext)
				.setContentTitle(msqTitle).setContentText(msqText)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent)
				.getNotification();
						
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(mContext.NOTIFICATION_SERVICE);
		// hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
	
		notificationManager.notify(1, noti);
	}
	public void setDeleteCacheFiles(){
		bDeleteCacheFiles = true;
	}
	
	public void addCacheFilesForDelete(String s){
		arrDeleteFiles.add(s);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	
		super.onCreate(savedInstanceState);
		
				
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		cacheVoteList = new ArrayList<CacheVoteBox>();
		
		setContentView(R.layout.main_act);
		//setContentView(R.layout.maintest);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		
	
		aplCtx = (ThatBetter)getApplicationContext();
		mContext = this;
		
		//Логин в парс.ком
		if (!ThatBetter.isLoggedIn) {								
			User currentUser = aplCtx.getUser();		
			try {
				currentUser.login(aplCtx,  this);
				ThatBetter.isLoggedIn = true;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		 ActionBar actionBar = getActionBar();
		 actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);//.NAVIGATION_MODE_TABS);
		 
		 //actionBar.setDisplayHomeAsUpEnabled(true);
		 actionBar.setDisplayShowTitleEnabled(false); //убираем заголовок приложения
		
		   	
		 
		 /*
		// найдем View-элементы
		btnCheck  = (ImageButton) findViewById(R.id.check_button);
		btnNewBox = (ImageButton) findViewById(R.id.new_box_button);
		btnProf   = (ImageButton) findViewById(R.id.profile_photo_button);
	    
	     // присваиваем обработчик кнопкам
		btnCheck.setOnClickListener(this);
		btnNewBox.setOnClickListener(this);
		btnProf.setOnClickListener(this);
		*/
		
		 
		 //Прочитаем локальный кеш просмотренных картинок
		Hashtable<String, Long> exclude = ThatBetter.db.loadCurrentUserVotes(true);
 		arrayExcludeVoteCache = new ArrayList<String>(exclude.keySet());
		
 		
		fManager = getFragmentManager();
		//fContainer = (FrameLayout) findViewById(R.id.fragmentContainer);
		
		//AsyncTask
		
		if (backgroundJob==null){
			backgroundJob = new BackgroundJob(); 
			backgroundJob.execute();
		}

		//openVote();	
		
		//входящий параметр из нотификации, откроем профиль
		 String menuFragment = getIntent().getStringExtra("menuFragment");
		 if (menuFragment != null){
			   if (menuFragment.equals("openProfile")){
				   openProfile();
			   }
		 }
		    		     


	}
	
	//удаляем файл которы скачали для отображения
    public void clearVoteCache() {
    	for (int i = 0; i < cacheVoteList.size(); i++) {			    		
    		CacheVoteBox currCache = cacheVoteList.get(i);
			
			if (currCache.getPathImg(1,aplCtx)!=null)
				LocalFile.deleteFile(currCache.getPathImg(1,aplCtx));
			
			if (currCache.getPathImg(2,aplCtx)!=null)
				LocalFile.deleteFile(currCache.getPathImg(2,aplCtx));								
		}
    	
    	cacheVoteList.clear();
    	
    }
	
    /*
    public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
		return super.onOptionsItemSelected(item);
	}
	*/
    
    @Override
    protected void onStart() {
    	//GA
		EasyTracker.getInstance(this).activityStart(this);
        super.onStart();
    }
    
    @Override
	protected void onStop() {
		if (isDEBUG)
			Log.d(TAG, "onStop ThatBetterActivity");
		//GA				
		EasyTracker.getInstance(this).activityStop(this);
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		if (isDEBUG)
			Log.d(TAG, "onDestroy ThatBetterActivity");
		// Завершить сохранение картинки сразу,
		// как закроется Activity

		if (backgroundJob != null) {
			backgroundJob.cancel(false);
			backgroundJob = null;
		}

		clearVoteCache();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret;
		if (item.getItemId() == R.id.item_check) {
	  		// Handle Settings
	  		ret = true;
	       // режим выбора
	       openVote();
	  		
	  	} 
	  	if (item.getItemId() == R.id.item_new_box) {
	  		// Handle Settings
	  		ret = true;
	  	    // новый запрос
		       newBox(); 
	  	}
	  	if (item.getItemId() == R.id.item_profile_photo) {
	  		// Handle Settings
	  		ret = true;
	  		openProfile();
	  	}
	  	else {
	  		ret = super.onOptionsItemSelected(item);
	  	}
	  	return ret;
	  }
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		  
		return super.onCreateOptionsMenu(menu);
		
	}
	
/*
	@Override
	public void onClick(View arg0) 
	{
		// по id определеяем кнопку, вызвавшую этот обработчик
	     switch (arg0.getId()) 
	     {
	     case R.id.check_button:
	       // режим выбора
	       openVote();
	       break;
	     case R.id.new_box_button:
	       // новый запрос
	       newBox(); 
	       break;
	     case R.id.profile_photo_button:
	       // профиль
		   
		   break; 
	     }
	}
*/
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//БЛОК
	//СОЗДАНИЕ НОВОГО БОКСА
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//новый запрос
	public void newBox() 
	{			
		/**		
		Работа с фрагментами
		http://habrahabr.ru/post/207036/
		http://developer.alexanderklimov.ru/android/theory/fragments.php	
		*/
		
		//Инициалзиация
		setImageNum(0);
		currentBoxID = UUID.randomUUID().toString();
		
		openCameraView();
	}
	
	
	public void setFragmentTransaction(int idContainer, Fragment setFragment, String fTag){
		FragmentTransaction fragmentTransaction = fManager.beginTransaction();
		//fragmentTransaction.replace(R.id.fragmentContainer, fragmentPreview,fTAG_preview);
		fragmentTransaction.replace(idContainer, setFragment,fTag);
		fragmentTransaction.commit();
	}
	
	
	public void openVote() {

		Fragment fragment = fManager.findFragmentByTag(fTAG_vote);

		if (fragment == null) {

			if (fragmentVote == null) {
				fragmentVote = new VoteFragment();
			}

			setFragmentTransaction(R.id.fragmentContainer, fragmentVote,
					fTAG_vote);
		}
	}
	
	
	public void openProfile()	{

		
		Fragment fragment = fManager.findFragmentByTag(fTAG_profile);

		if (fragment == null) {

			if (fragmentProfile == null) {
				fragmentProfile = new ProfileFragment();
			}

			setFragmentTransaction(R.id.fragmentContainer, fragmentProfile,
					fTAG_profile);
		}
	}
	
	
	public void openPreviewView() 
	{		
		boolean allFoto;
	
		if (getImageNum()==1)
			allFoto =false;
		else
			allFoto =true;
		
		if (allFoto){
			//показать превью двух фото, перед отправкой 
			//fragment_preview_all
			Fragment fragment = fManager.findFragmentByTag(fTAG_previewAll);

			if (fragment == null) {
								 
				ImagePreviewAll fragmentPreviewAll = new ImagePreviewAll();				
				setFragmentTransaction(R.id.fragmentContainer, fragmentPreviewAll,fTAG_preview);				
			}			
		}
		else{
			Fragment fragment = fManager.findFragmentByTag(fTAG_preview);

			if (fragment == null) {
				
				if (fragmentPreview== null){ 
					fragmentPreview = new ImagePreview();
				}
				
				setFragmentTransaction(R.id.fragmentContainer, fragmentPreview,fTAG_preview);				
			}			
		}
							
	}
	
	
	public void openCameraView() 
	{
			
		Fragment fragment = fManager.findFragmentByTag(fTAG_camera);
		if (fragment == null) {
			fragmentCamera = new CameraFragment();				
			setFragmentTransaction(R.id.fragmentContainer, fragmentCamera,fTAG_camera);
		}
		
		/*
		 * 08.05.2014 нужно ли тут создавать два объекта ???
		if (fragment == null) {
			
			if (fragmentCamera== null){ 
				fragmentCamera = new CameraFragment();				
				setFragmentTransaction(R.id.fragmentContainer, fragmentCamera,fTAG_camera);
				
			}
			else
			{ 				
				CameraFragment fragmentCamera2 = new CameraFragment();				
				setFragmentTransaction(R.id.fragmentContainer, fragmentCamera2,fTAG_camera);
			}
						
		} 
		*/		
	}
	
	
	public void processingImageDate(byte[] data, boolean isCamera){
		//Toast.makeText(getActivity(), R.string.save_image, Toast.LENGTH_LONG).show();
				Toast.makeText(
                        this,
                        String.format(Locale.getDefault(), "%s", getString(R.string.image_process)),
                        Toast.LENGTH_SHORT).show();
				
				//Работа с гл. переменными 						
				int curImgNum = getImageNum()+1; 
				setImageNum(curImgNum); 
						
				ImageManager im = new ImageManager(this, 1400, 1400);
				Bitmap bm = im.setIsScale(true)
				              .setIsResize(true)
				              .setIsCrop(false)
				              .getFromByteArray(data, isCamera);
				
				//Декодируем в байты
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);
				byte[] scaledData = bos.toByteArray();
				//второй способ BitmapFactory.decodeByteArray(bitmap , 0, bitmap.length);
				
				
				/*
				 * СТАРЫЙ ВАРИАНТ
				// Resize photo from camera byte array
				Bitmap boxImage = BitmapFactory.decodeByteArray(data, 0, data.length);
				Bitmap boxImageScaled = Bitmap.createScaledBitmap(boxImage, 1400, 1400
						* boxImage.getHeight() / boxImage.getWidth(), false);
						
				boxImage.recycle();
				
				// Override Android default landscape orientation and save portrait
				//работа с матрицей, если нужно повернуть картинку http://developer.alexanderklimov.ru/android/catshop/matrix.php#postrotate		
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				Bitmap rotatedScaledBoxImage = Bitmap.createBitmap(boxImageScaled, 0,
						0, boxImageScaled.getWidth(), boxImageScaled.getHeight(),
						matrix, true);
				
				boxImageScaled.recycle();
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				rotatedScaledBoxImage.compress(Bitmap.CompressFormat.JPEG, 70, bos);
				
				rotatedScaledBoxImage.recycle();
				
				byte[] scaledData = bos.toByteArray();
				
				*/
				
				
				//Toast.makeText(getActivity(), "Запись локально нач", Toast.LENGTH_SHORT).show();
				//пробуем записать	
				
				//контекст для записи во внут. память
				Context appCont = (ThatBetter) getApplicationContext();
						
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Context", appCont);
				map.put("scaledData", scaledData);
				map.put("currentImgNum", curImgNum);
				if (isDEBUG) Log.d(TAG, "map.put currentImgNum"+String.valueOf(curImgNum));
				map.put("Type", AsyncTaskType.SAVE_IMG);
				saveFileAsyncTask(map);				
				
				/*
				 * сохранение не в потоке
				//новый объект для записи файла
				LocalFile lfile = new LocalFile();			
				String sFileName = lfile.writeFile(scaledData, appCont);
				//сохраним имя файлв для установки превью и передачи в кеш
				act.setImageFileName(currentImgNum,sFileName);
				*/
				
				//Toast.makeText(getActivity(), "ParseFile start", Toast.LENGTH_SHORT).show();
				
				/*
				
				// Save the scaled image to Parse
				photoFile = new ParseFile("box_photo.jpg", scaledData);
				photoFile.saveInBackground(new SaveCallback() {

					public void done(ParseException e) {
						if (e != null) {
							Toast.makeText(getActivity(),
									"Error saving: " + e.getMessage(),
									Toast.LENGTH_LONG).show();
						} else {				
							ThatBetterActivity act = (ThatBetterActivity) getActivity();							
							act.setParseFile(act.getImageNum(), photoFile);
							//addPhotoToMealAndReturn(photoFile);
								
						}
					}
				});
				
				*/
				
				//Toast.makeText(getActivity(), "ParseFile finish", Toast.LENGTH_SHORT).show();	
						
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//отправляем новый бокс на сервер
	//////////////////////////////////////////////////////////////////////////////////////////////
	private void sendBox() 	
	{

		
		if (isDEBUG) Log.d(TAG, "sendBox() start");
		
//		ThatBetter appCont = (ThatBetter) getApplicationContext();
		 
		Box nBox = new Box(); 
		nBox.setImage1(pPhotoFile1);
		nBox.setImage2(pPhotoFile2);
		nBox.setBoxID(currentBoxID);
		nBox.setAuthor(ParseUser.getCurrentUser());
			// Save the meal and return
		nBox.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						Toast.makeText(
								getApplicationContext(),
								R.string.success,
								Toast.LENGTH_SHORT).show();
						//getActivity().setResult(Activity.RESULT_OK);
						//getActivity().finish();
						Log.d(TAG, "sendBox() ок");
					} else {
						Toast.makeText(
								getApplicationContext(),
                                String.format(Locale.getDefault(), "%s%s", getString(R.string.save_error), e.getMessage()),
								Toast.LENGTH_SHORT).show();
						Log.d(TAG, "sendBox() "+e.getMessage());
					}
				}

			});
		
		// save box to local store
		LocalBox lBox = new LocalBox();
		lBox.id = currentBoxID;
		lBox.imageFileName1 = sLocalFileName1;
		lBox.imageFileName2 = sLocalFileName2;
		lBox.Votes1 = (long) 0;
		lBox.Votes2 = (long) 0;
		ThatBetter.db.addLocalBox(lBox);
		
		//добавляем в кеш просмотеренных боксов
		arrayExcludeVoteCache.add(currentBoxID);
	}
	

	public void iWantSendBox() 	
	{
		if (isDEBUG) Log.d(TAG, "iWantSendBox() start");
		
		isReadyForSendBox = true;
	
		// make listview with pictures from localBox
			try {
				ThatBetter.db.loadLocalBoxes(true);

				// ProfileFragment
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			openVote();
		 
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    super.onActivityResult(requestCode, resultCode, data);

	    switch(requestCode)
	    {
	         case 1:
	         {
	             if (resultCode == RESULT_OK)
	             {
	                 Uri chosenImageUri = data.getData();

                     try {
                         InputStream iStream =   getContentResolver().openInputStream(chosenImageUri);
                         byte[] inputData = LocalFile.StreamToByte(iStream);
                         processingImageDate(inputData,false);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     openPreviewView();
	             }
	             break;
	         }
	    }
	}

	public void imgSelect(){
		//автор http://hashcode.ru/questions/149501/java-android-%D0%B2%D1%8B%D0%B1%D0%BE%D1%80-%D1%84%D0%B0%D0%B9%D0%BB%D0%B0
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 1);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//Методы для работы с переменными класса, использымыми для отслеживания состояния создания нового бокса
    ////////////////////////////////////////////////////////////////////////////////////////////
	public int getImageNum()
	{
	   return iImageNum;
	}
	
	public void setImageNum(int i)
	{
	   iImageNum = i;
	}
	
	public void setImageFileName(int i, String sFileName)
	{
		if(i==1){
			sImageFileName1	= sFileName;
		}
		else{
			sImageFileName2	= sFileName;
		}	   	   
	}
	
	public String getImageFileName(int i)
	{
		if(i==1){
			return sImageFileName1;
		}
		else{
			return sImageFileName2;
		}	   
	}
	
	public void setParseFile(int i, ParseFile photoFile)
	{
		if (isDEBUG) Log.d(TAG, "setParseFile, i ="+String.valueOf(i));
		if(i==1){
			pPhotoFile1	= photoFile;
		}
		else{
			pPhotoFile2	= photoFile;
		}	   	   
	}
	
	
	
	public byte[] getBytePhoto(int i)
	{
		if(i==1){
			return bytePhotoFile1;
		}
		else{
			return bytePhotoFile2;
		}	   
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//AsyncTask
//АВТОР: http://habrahabr.ru/company/eastbanctech/blog/192998/
//http://developer.alexanderklimov.ru/android/theory/asynctask.php
////////////////////////////////////////////////////////////////////////////////////////////
	
	private void saveFileToParseCom(final ParseFile photoFile) {
	// Save the scaled image to Parse
	//photoFile = new ParseFile("box_photo"+String.valueOf(i)+".jpg", scaledData);
	photoFile.saveInBackground(new SaveCallback() {	
		public void done(ParseException e) {
			if (e != null) {
				
				Toast.makeText(getApplicationContext(),
                        String.format(Locale.getDefault(), "%s%s", getString(R.string.save_error), e.getMessage()),
						Toast.LENGTH_LONG).show();
						
			} else {
/*
				Toast.makeText(
						getApplicationContext(),
						"parse.com: "+photoFile.getName(),
						Toast.LENGTH_SHORT).show();
*/
				if (isDEBUG) Log.d(TAG, "photoFile.saveInBackground photoFile.getName() - "+photoFile.getName());
				if (photoFile.getName().indexOf("box_photo1.jpg")!=-1)															
					setParseFile(1, photoFile);
				else
					setParseFile(2, photoFile);						
			}
		}
	});
	}
	
	public void saveFileAsyncTask(HashMap map)
	{										
		//запишием переменные
		byte[] scaledData = (byte[]) map.get("scaledData");    	
    	int i = (Integer) map.get("currentImgNum");
    	
    	if (isDEBUG) Log.d(TAG, "saveFileAsyncTask "+i);
    	
    	if(i==1){    	    		
    		bytePhotoFile1	= scaledData;    		
    		needSavePhotoFile1 = true;    		
		}
		else{
			bytePhotoFile2	= scaledData;
			needSavePhotoFile2 = true;		
		}
    	
    	saveFileToParseCom(new ParseFile("box_photo"+String.valueOf(i)+".jpg", scaledData));
    	    	    		   	   
	}
	
	public ArrayList getCacheVoteList(){
		return cacheVoteList; 
	}
	
	public static void notifyVoteCacheNewRecordLoaded() {
		//основа для реализации 
		//http://habrahabr.ru/post/130977/
		
		 final Intent intent = new Intent(CocsIntents.VOTECACHE_NEW_RECORD);		 		 
		 ThatBetter.getInstance().sendBroadcast(intent);
	   }
	  
	
////////////////////////////////////////////////////////////////////////////////
	void deleteLocalCacheFiles(){
		
		ArrayList<String> copy = new ArrayList<String>(arrDeleteFiles);
		for (String item : copy) {
			if (item!=null){
				//if (isDEBUG)
					//Log.d(TAG,
						//	"VoteFragment LocalFile.deleteFile"+item);
				LocalFile.deleteFile(item);				
								
				//String pos = arrDeleteFiles.get(copy.indexOf(item));
				arrDeleteFiles.set(copy.indexOf(item), null);
				
				bDeleteCacheFiles = false;
			}	
		}	
		
		/*
		for (String item : arrDeleteFiles) {
			if (!item.equals("")){
				if (isDEBUG)
					Log.d(TAG,
							"VoteFragment LocalFile.deleteFile"+item);
				LocalFile.deleteFile(item);
				item = "";
				bDeleteCacheFiles = false;
			}	
			

		}	
		*/		
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// ПОТОКИ
    //////////////////////////////////////////////////////////////////////////////
	 /*
	  * 	http://habrahabr.ru/post/187854/
			http://www.ibm.com/developerworks/ru/library/j-jtp10264/index.html
			http://habrahabr.ru/post/130977/ синхронизации потоков с использованием Intent, BroadcastReceiver и Handler. 
    */
   
    class BackgroundJob extends AsyncTask<HashMap, Integer, String>{
    	int cacheSize = 3;    	
    	
    	public void getDateOnParseCom(){
    		
    		//кеш заполнен
			if (cacheVoteList.size()>= cacheSize){				
				return;
			}
			
    		isWorkFillingCacheVoteList = true;
    		
    		ParseQuery<Box> myQuery = Box.getQuery();
    		myQuery.setLimit(cacheSize);
    		// query.whereLessThanOrEqualTo("ver", 1);
    		myQuery.orderByAscending("Rank");
    		myQuery.orderByDescending("createdAt");
    		myQuery.setLimit(cacheSize); 
    		
    		myQuery.whereNotContainedIn("boxID", arrayExcludeVoteCache);
    		myQuery.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
    		
    		myQuery.findInBackground(new FindCallback() {
    		@Override
    		public void done(List boxList, ParseException e) {    				
    				
    				if (e == null) {    					
    					final Context ctx = (ThatBetter)getApplicationContext();
    					    					
    					int countList = boxList.size(); //количество записей
    					
    					if (countList==0){    						
    						isWorkFillingCacheVoteList = false;
    						ThatBetter.shownAllBox = true;    						
        					return;
    					}
    					
    					Iterator<ParseObject> i = boxList.iterator();    					
    					while (i.hasNext()) {
    						final CacheVoteBox voteCache= new CacheVoteBox();
    						
    						ParseObject boxObj = i.next();

    						voteCache.setBoxId(boxObj.getObjectId());
    						
    						String sCocsID = ((Box)boxObj).getBoxID();
    						voteCache.setCocsBoxId(sCocsID);
    						cacheVoteList.add(voteCache); //добавляем в массив для просмотра
    						
    						arrayExcludeVoteCache.add(sCocsID);
    						
    						if (isDEBUG) Log.d("VoteCacheDebug","Get from parse boxID:"+sCocsID); //тестирование кеша

    						ParseFile PFile1 = boxObj.getParseFile("Img1");						
    						if (PFile1 != null) {
    							PFile1.getDataInBackground(new GetDataCallback() {
    								  public void done(byte[] data, ParseException e) {
    									    if (data != null) {    									    				    							        		
    							        		voteCache.setPathImg(LocalFile.writeFile(data, ctx, null), 1);
    							        		//проверим если загрузили уже вторую картинку, отправим сообщение, что кеш готов
    							        		if (voteCache.getPathImg(2, aplCtx)!=null)
    							        			notifyVoteCacheNewRecordLoaded();
    									    }
    									  }
    									});
    						}

    						ParseFile PFile2 = boxObj.getParseFile("Img2");
    						if (PFile2 != null) {
    							PFile2.getDataInBackground(new GetDataCallback() {
  								  public void done(byte[] data, ParseException e) {
									    if (data != null) {									    				
									    	voteCache.setPathImg(LocalFile.writeFile(data, ctx, null), 2);
									    	//проверим если загрузили уже первую картинку, отправим сообщение, что кеш готов
							        		if (voteCache.getPathImg(1, aplCtx)!=null)
							        			notifyVoteCacheNewRecordLoaded();
									    }
									  }
									});
    						}
    						
    						//кеш заполнили
    						if (cacheVoteList.size()>= cacheSize){
    							isWorkFillingCacheVoteList = false;
    							break;
    						}    						
    						
    						    						
    					}

    				} else {
    					isWorkFillingCacheVoteList = false;
    					Toast.makeText(
								getApplicationContext(),
                                String.format(Locale.getDefault(), "%s", getString(R.string.receive_error)),
								Toast.LENGTH_SHORT).show();
    					    					
    					
    				}
    			}

    		});
    	
    		
    	}
    	
        @Override
        //выполняется перед doInBackground, имеет доступ к UI
        protected void onPreExecute() {    
        	super.onPreExecute();
        }

        @Override
        protected String doInBackground(HashMap... params) {  
        	int countSek = 0;
            int countVoteUpdate = 0;
        	
			while (true) {

				if (isCancelled())
					break;

				//запись первой фотки
				if (needSavePhotoFile1 && bytePhotoFile1!=null){
					if (isDEBUG) Log.d(TAG,"BackgroundJob LocalFile.writeFile()");
					needSavePhotoFile1 = false;
					ThatBetter aplCtx = (ThatBetter)getApplicationContext();										
			    	//sLocalFileName1 = LocalFile.writeFile(bytePhotoFile1, aplCtx, currentBoxID+"1.jpg");
					sLocalFileName1 = LocalFile.writeFile(bytePhotoFile1, aplCtx, null);
				}	
		        		
				//запись второй фотки
				if (needSavePhotoFile2 && bytePhotoFile2!=null){
					if (isDEBUG) Log.d(TAG,"SaveImgLocal LocalFile.writeFile()");
					needSavePhotoFile2 = false;
					ThatBetter aplCtx = (ThatBetter)getApplicationContext();										
			    	sLocalFileName2 = LocalFile.writeFile(bytePhotoFile2, aplCtx, null);
				}	
			    		
				//обновление кеша
				if (ThatBetter.isLoggedIn && cacheSize > cacheVoteList.size() && !isWorkFillingCacheVoteList && !ThatBetter.shownAllBox) {
					if (isDEBUG) Log.d(TAG,"BackgroundJob getDateOnParseCom()");
					getDateOnParseCom();					
					}
					
				//отправка бокса
				if (isReadyForSendBox && pPhotoFile1!=null && pPhotoFile2!=null && pPhotoFile1.isDataAvailable() && pPhotoFile2.isDataAvailable()){        			
					isReadyForSendBox = false;
					if (isDEBUG) Log.d(TAG,"BackgroundJob sendBox()");
        			sendBox();
        			}
				
				if (bDeleteCacheFiles){
					deleteLocalCacheFiles();
				}
				
				if (countVoteUpdate == 1800) {
                    countVoteUpdate = 0;
                    TBDB.updateVotesInLocalDB(mContext);
                }
								
					try {
						if (isDEBUG) Log.d(TAG, "BackgroundJob TimeUnit.SECONDS.sleep(1)");
						TimeUnit.SECONDS.sleep(1);

                        // счетчик секунд когда обновляем голоса для данного пользователя.
                        countVoteUpdate++;

						//считаем количество циклов простоя если вытянули все боксы
						if (ThatBetter.shownAllBox)
							countSek = countSek +1;
						
						//после часа ожидания пробуем еще раз получить боксы с сервака
						if (ThatBetter.shownAllBox && countSek==3600)
							ThatBetter.shownAllBox=false;							
							
						//обнулим счетчик
						if (!ThatBetter.shownAllBox)
							countSek = 0;
							
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						if (isDEBUG) Log.d(TAG,"BackgroundJob TimeUnit InterruptedException e");
						e.printStackTrace();
					}
				

			}

			return "";
		}
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            //progressDialog.setProgress(progress[0]);
        }
        
        //Скроем диалог и покажем картинку
        // выполняется после doInBackground (не срабатывает в случае, если AsyncTask был отменен), имеет доступ к UI
        @Override
        protected void onPostExecute(String result) {        	
        }
        
        //Этот метод будет вызван вместо onPostExecute,
        //если мы остановили выполнение задачи методом 
        //AsyncTask#cancel(boolean mayInterruptIfRunning)
        @Override
        protected void onCancelled() {

        }
    }
}




