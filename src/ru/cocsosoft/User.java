package ru.cocsosoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class User extends ParseUser
{
	//http://habrahabr.ru/post/116719/
    private String sID = null;
    private boolean fNewUser = false;
    protected ProgressDialog proDialog;
    
    public User() 
	{
		
	}
    
	public boolean login(Application context, final Context actCtx) throws ParseException
	{
		boolean fSuccess = false;
		
		
		
		//получаем ИД устройства
		String DeviceID = userId(context);
		//Log.d(TAG, DeviceID); //в лог на проверку
		
		
		// Add your initialization code here
		Parse.initialize(context, "OF7F799p0yrIo7QZiMHxIFS7F83ZFHrFHdZcaww6", "phqwogcR3TFYjqT0xVfv4rvykNN65t29FOe9gS1A");


		enableAutomaticUser();
		//ParseACL defaultACL = new ParseACL();
	    
		// If you would like all objects to be private by default, remove this line.
		//defaultACL.setPublicReadAccess(true);
		
		//ParseACL.setDefaultACL(defaultACL, true);
		

	   //готовим юзера к отправке 
		setUsername(DeviceID);
		setPassword(DeviceID);
		setEmail(DeviceID+"@cocsosoft.com");
		  
		// пример добавления еще полей
		//user.put("phone", "650-555-0000");

		
		
		if (fNewUser)
		{
			signUpInBackground ( 
					new SignUpCallback() 
					{
						  public void done(ParseException e) 
						  {
								if (e == null)
								{
								  // Hooray! Let them use the app now.
									
								}
								else
								{
								  // Sign up didn't succeed. Look at the ParseException
								  // to figure out what went wrong
								}
						  }
					}
			);
		}
		else
		{

			//http://stackoverflow.com/questions/15299921/parse-com-android-api-and-android-dialog
			//!!! переписать
			
			startLoading(actCtx);
			
		    ParseUser.logInInBackground(DeviceID, DeviceID, new LogInCallback() {
		        public void done(ParseUser user, ParseException e) {
		            if (user != null) {		            	
		                stopLoading();	
		                ((ThatBetterActivity) actCtx).openVote();
		            } else {
		                stopLoading();
		                //invalidCreds();
		            }
		        }
		    }); 
			
			/*
			try {
				logIn(DeviceID, DeviceID);												
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		

		/*
		ParseACL defaultACL = new ParseACL();
		// Optionally enable public read access by default.
		// defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
		*/
		
		
		return fSuccess;
	}
	
	protected void startLoading(Context context) {
					
	    proDialog = new ProgressDialog(context);	    
	    proDialog.setMessage(context.getString(R.string.loading));
	    proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    proDialog.setCancelable(false);
	    proDialog.show();
	    
	}

	protected void stopLoading() {
		
	    proDialog.dismiss();
	    proDialog = null;	    
	}

    private LogInCallback LogInCallback() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized String userId(Context context) 
    {
        if (sID != null)
        	return sID;

        File userIdFile = new File(context.getFilesDir(), "user.id");
        try 
        {
            if (!userIdFile.exists())
            {
                writeInstallationFile(userIdFile);
                fNewUser = true;
            }
            
            sID = readInstallationFile(userIdFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return sID;
    }

    private String readInstallationFile(File installation) throws IOException 
    {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private void writeInstallationFile(File installation) throws IOException 
    {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}

