package ru.cocsosoft;

import android.app.Application;

import com.parse.ParseObject;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


@ReportsCrashes(formKey = "", // will not be used
mailTo = "cocsosoft@gmail.com",
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.crash_toast_text)


public class ThatBetter extends Application {

	private User currentUser;
	public static String sBaseAppDataPath = "/ThatBetter";
	public static TBDB db;
	public static boolean isLoggedIn = false;
	private static ThatBetter mInstance;
	volatile static boolean shownAllBox = false;

	 /***
     * Выдает экземпляр приложения
     */
    public static ThatBetter getInstance() {
        return mInstance;
    }
    
	public User getUser() {
		return currentUser;
	}

	//private TBDB db = new TBDB(getApplicationContext());
	//HashSet<String> sss = db.loadCurrentUserVotes();

	// ////////////////////////////////////////
	// глобальные переменные
	// http://www.cyberforum.ru/android-dev/thread630359.html
	// http://rappasocial.com/content/android-%D0%BA%D0%B0%D0%BA-%D0%B8%D1%81%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D1%8C-%D0%B3%D0%BB%D0%BE%D0%B1%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B5-%D0%BF%D0%B5%D1%80%D0%B5%D0%BC%D0%B5%D0%BD%D0%BD%D1%8B%D0%B5
	// ////////////////////////////////////////

	@Override
	public void onCreate() {
				    
		super.onCreate();
		 
		// The following line triggers the initialization of ACRA
        ACRA.init(this);
        
		mInstance = this;
		 
		ParseObject.registerSubclass(User.class);
		ParseObject.registerSubclass(Box.class);
		ParseObject.registerSubclass(Vote.class);
		
		currentUser = new User();

		/*
		 * перекинул в ThatBetterActivity try { currentUser.login(this); // open
		 * database to operate farther //db.openDB(); } catch (ParseException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// ParseAnalytics.trackAppOpened(getIntent());
		
		db = new TBDB(getApplicationContext());
	}

}
