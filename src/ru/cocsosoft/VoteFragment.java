package ru.cocsosoft;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.ArrayList;


public class VoteFragment extends Fragment implements OnClickListener,
		AnimationListener {
	Context appCont;
	private static final String TAG = "DebugLogs"; // для тех. логов
	private static final boolean isDEBUG = true; // вкл. логов
	private ParseFile pPhotoFile1;
	private ParseFile pPhotoFile2;
	private ImageView imageView1;
	private ImageView imageView2;
	private ProgressBar progressBar;
	private TextView textView_load_date;
	private TextView textViewHelp1;
	private TextView textViewHelp2;
	private String currentBoxId;
	private String currentCocsBoxId;
	ThatBetterActivity akt;
	Animation animationFalling;
	LinearLayout linearLayout;
	Animation animationRotate;
	Animation animationShake;

	Animation animationFlipIn;
	Animation animationFlipOut;

	ImageButton btnNextBox;
	private final static int MESSAGE_CACHE_LOADED = 1;
	
	
	

	// используем для контроля, что у нас запущена загрукза бокса только один
	// раз в текущее время
	boolean bDontRunLoadBox = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {

		bDontRunLoadBox = false;
		
		appCont = (ThatBetter) getActivity().getApplicationContext();
		akt = (ThatBetterActivity) getActivity();
		animationFlipIn = AnimationUtils.loadAnimation(appCont,
				android.R.anim.slide_in_left);
		animationFlipOut = AnimationUtils.loadAnimation(appCont,
				android.R.anim.slide_out_right);
		animationFlipOut.setAnimationListener(this);

		View v = inflater.inflate(R.layout.vote_box, null);

		// анимация от сюда
		// http://developer.alexanderklimov.ru/android/animation/translate.php
		animationFalling = AnimationUtils
				.loadAnimation(appCont, R.anim.falling);
		animationFalling.setAnimationListener(this);

		animationRotate = AnimationUtils.loadAnimation(appCont,
				R.anim.rotate_center);
		animationRotate.setAnimationListener(this);

		animationShake = AnimationUtils.loadAnimation(appCont, R.anim.shake);
		animationShake.setAnimationListener(this);

		btnNextBox = (ImageButton) v.findViewById(R.id.next_box_vote_button);
		btnNextBox.setOnClickListener(this);

		imageView1 = (ImageView) v.findViewById(R.id.icon1);
		imageView1.setOnClickListener(this);
		imageView2 = (ImageView) v.findViewById(R.id.icon2);
		imageView2.setOnClickListener(this);
		progressBar = (ProgressBar) v.findViewById(R.id.progress_bar_vote);
		textView_load_date = (TextView) v
				.findViewById(R.id.text_view_load_date_vote);
		textViewHelp1 = (TextView) v.findViewById(R.id.text_view_help_text1);
		textViewHelp2 = (TextView) v.findViewById(R.id.text_view_help_text2);
		textViewHelp1.setVisibility(View.INVISIBLE);
		textViewHelp2.setVisibility(View.INVISIBLE);

		linearLayout = (LinearLayout) v.findViewById(R.id.LinearLayoutVote);

		// http://habrahabr.ru/post/119023/
		textViewHelp1.setVisibility(View.INVISIBLE);
		textViewHelp2.setVisibility(View.INVISIBLE);

		// сразу запускаем показ картинки
		loadNextBox();

		return v;
	}

	public void changeImage(boolean bOut) {
		if (bOut) {
			еnableImgView(false);
			textViewHelp1.setVisibility(View.INVISIBLE);
			textViewHelp2.setVisibility(View.INVISIBLE);
		} else {
			imageView1.setVisibility(View.VISIBLE);
			imageView2.setVisibility(View.VISIBLE);

			textViewHelp1.setVisibility(View.VISIBLE);
			textViewHelp2.setVisibility(View.VISIBLE);

			imageView1.startAnimation(animationFlipIn);
			imageView2.startAnimation(animationFlipIn);

			еnableImgView(true);

			
		}
		// удалим файлы с кеша!!! 
		akt.setDeleteCacheFiles();	
	}

	@Override
	public void onClick(View v) {
		bDontRunLoadBox = false;			
		
		switch (v.getId()) {
		case R.id.next_box_vote_button: {
			
			loadNextBox();
			break;
		}
		case R.id.icon1: {

			changeImage(true);
			imageView1.startAnimation(animationShake);
			voteImg(1);
			break;

		}
		case R.id.icon2: {
			changeImage(true);
			imageView2.startAnimation(animationShake);
			voteImg(2);
			break;
		}
		}
	}

	public void voteImg(final int numImg) {
		if (currentBoxId == null)
			return;

		// Toast.makeText(appCont, "Ваш голос учтён!",
		// Toast.LENGTH_SHORT).show();
		// !!! Запись в базу
		ThatBetter.db.addVote(currentCocsBoxId);

		ParseQuery<Box> myQuery = Box.getQuery();
		myQuery.getInBackground(currentBoxId, new GetCallback() {
			@Override
			public void done(ParseObject object, ParseException e) {
				if (object != null) {
					((Box) object).updateRank(numImg);
					object.saveInBackground();
				} else {
					// Не найден
					// Toast.makeText(appCont, "Не найден",
					// Toast.LENGTH_SHORT).show();
				}
			}
		});		
	}

	public void startProgressBar() {
		btnNextBox.setEnabled(false);

		progressBar.setVisibility(View.VISIBLE);
		textView_load_date.setVisibility(View.VISIBLE);

		setVisibleImgGroup(false);
	}

	public void stopProgressBar() {
		btnNextBox.setEnabled(true);

		// спрячем статус бар
		progressBar.setVisibility(View.INVISIBLE);
		textView_load_date.setVisibility(View.INVISIBLE);

		setVisibleImgGroup(true);
	}

	// вкл. отображение картинок
	public void еnableImgView(boolean bDisable) {
		imageView1.setEnabled(bDisable);
		imageView2.setEnabled(bDisable);
	}

	// отображение картинок с текстом
	void setVisibleImgGroup(boolean bVisible) {
		int mode;

		if (bVisible) {
			mode = View.VISIBLE;
		} else {
			mode = View.INVISIBLE;
		}
		imageView1.setVisibility(mode);
		imageView2.setVisibility(mode);

		textViewHelp1.setVisibility(mode);
		textViewHelp2.setVisibility(mode);
	}

	public void loadNextBox() {

		if (bDontRunLoadBox)
			return;

		bDontRunLoadBox = true;

        if (!ThatBetter.shownAllBox) {
            // Покажем статус бар
            startProgressBar();
        }

		ArrayList<CacheVoteBox> cacheVoteList = akt.getCacheVoteList();
		if ((cacheVoteList == null) || (cacheVoteList.size() == 0)) {

			if (ThatBetter.shownAllBox) {
				// показали все картинки которые есть, выдаем сообщение и выкл.
				// возможность получения картинок
				stopProgressBar();
				setVisibleImgGroup(false);
				Toast.makeText(getActivity(), getString(R.string.shownAllBox),
						Toast.LENGTH_LONG).show();
			}

			bDontRunLoadBox = false;
			return;
		}

		for (int i = 0; i < cacheVoteList.size(); i++) {
			CacheVoteBox currCache = cacheVoteList.get(i);

			String fullPath1 = currCache.getPathImg(1,appCont);
			String fullPath2 = currCache.getPathImg(2,appCont);
			
			if (fullPath1 == null
					|| fullPath2 == null)
				continue;

            if (new File(fullPath1).exists() == false
                    || new File(fullPath2).exists() == false )
                continue;


            if (isDEBUG) Log.d("VoteCacheDebug", "fullPath1: " + fullPath1 + "File 1 exist: " + new File(fullPath1).exists() );
            if (isDEBUG) Log.d("VoteCacheDebug", "fullPath2: " + fullPath2 + "File 2 exist: " + new File(fullPath2).exists());



			// остановим прогресс бар
			stopProgressBar();

			currentBoxId = currCache.getBoxId();
			currentCocsBoxId = currCache.getCocsBoxId();

			if (isDEBUG)
				Log.d("VoteCacheDebug", "Get from local cache boxID:"
						+ currentCocsBoxId); // тестирование кеша

			// показываем превью из сохраненого файла			
			imageView1.setImageDrawable(Drawable.createFromPath(fullPath1));
			
			imageView2.setImageDrawable(Drawable.createFromPath(fullPath2));

			changeImage(false);

			// удаляем из кеша
			cacheVoteList.remove(i);
			
			//добавим файлы к удалению
			
			akt.addCacheFilesForDelete(fullPath1);
			akt.addCacheFilesForDelete(fullPath2);			
			break;
		}
	

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		ThatBetter.getInstance().unregisterReceiver(mNotificationReceiver);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MESSAGE_CACHE_LOADED:

				loadNextBox();

				break;

			default:
				break;
			}

			super.handleMessage(msg);

		}

	};

	/***
	 * Перехватчик событий. Переправляем его в обработчик для запуска в UI треде
	 */
	private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (CocsIntents.VOTECACHE_NEW_RECORD.equalsIgnoreCase(action)) {
				// stopProgressBar();
				if (isDEBUG)
					Log.d(TAG,
							"VoteFragment получили сообщение о загрузке в кеш");
				mHandler.sendEmptyMessageDelayed(MESSAGE_CACHE_LOADED, 250);
				// mHandler.removeMessages(MESSAGE_IMAGE_LOADED);
			}

		}
	};

	// ////////////////////////////////////////////////////////////////
	// РАБОТА С АНИМАЦИЕЙ
	@Override
	public void onAnimationStart(Animation animation) {
		// mImage.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// mImage.setVisibility(View.INVISIBLE);
		// покажем следующий объект для оценки
		if (animation == animationFlipOut) {
			imageView1.setVisibility(View.INVISIBLE);
			imageView2.setVisibility(View.INVISIBLE);
			textViewHelp1.setVisibility(View.INVISIBLE);
			textViewHelp2.setVisibility(View.INVISIBLE);
			loadNextBox();
		}

		if (animation == animationShake) {
			imageView1.startAnimation(animationFlipOut);
			imageView2.startAnimation(animationFlipOut);
		}

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// mImage.setVisibility(View.VISIBLE);
	}
	
	// ////////////////////////////////////////////////////////////////
	
	 @Override
	public void onResume() {
		super.onResume();				
	
		IntentFilter f = new IntentFilter();
		f.addAction(CocsIntents.VOTECACHE_NEW_RECORD);
		ThatBetter.getInstance().registerReceiver(mNotificationReceiver, f);
		
	}




}
