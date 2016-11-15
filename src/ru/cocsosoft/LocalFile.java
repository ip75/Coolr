package ru.cocsosoft;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

//http://startandroid.ru/ru/uroki/vse-uroki-spiskom/138-urok-75-hranenie-dannyh-rabota-s-fajlami.html
//http://habrahabr.ru/sandbox/38101/

//переписать в поток!!!
//http://habrahabr.ru/company/eastbanctech/blog/192998/
//http://startandroid.ru/ru/uroki/vse-uroki-spiskom/148-urok-85-esche-neskolko-sposobov-vypolnenija-koda-v-ui-potoke

public class LocalFile {
	private static final String sPrefSD = "sd";
	private static final String sPrefIS = "in";
	private static final String TAG = "DebugLogs"; // для тех. логов
	private static final boolean isDEBUG = true; // вкл. логов

	static private String newFilename(boolean isSD) {
		String filename;

		String id = UUID.randomUUID().toString();

		if (isSD) {
			filename = sPrefSD + id + ".jpg";
		} else {
			filename = sPrefIS + id + ".jpg";
		}

		return filename;
	}

	
	static public String appDir() {

		// получаем путь к SD
		File sdPath = Environment.getExternalStorageDirectory();
		// добавляем свой каталог к пути
		String path = sdPath.getAbsolutePath() + ThatBetter.sBaseAppDataPath;
		sdPath = new File(path);

		if (!sdPath.exists()) {
			// создаем каталог
			sdPath.mkdirs();
		}

		return path;
	}

    static public byte[] StreamToByte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

	static public byte[] fileToByte(String path) {
		File file = new File(path);
		int size = (int) file.length();
		byte[] bytes = new byte[size];
		try {
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

	static public String appDirToFileName(String fileName, Context ctx) {

		if (fileName == null)
			return null;

		// прочитаем префикс файла
		char[] buf = new char[2];
		fileName.getChars(0, 2, buf, 0);
		String pref = new String(buf);

		if (pref.equals(sPrefSD))
			return appDir() + "/";
		else{
			ContextWrapper cw = new ContextWrapper(ctx);
		    File directory = cw.getCacheDir();
		
		    return  directory.getAbsolutePath()+"/";
		}
		
	}

	static public String getFullPath(String fileName, Context ctx) {
		
		return appDirToFileName(fileName,ctx) + fileName;
	}

	static public void deleteFile(String filePath) {
		if (filePath != null) {
			File delF = new File(filePath);
			delF.delete();
		}

	}

	static public String writeFile(byte[] scaledData, Context ctx,
			String fileName) {
		if (isDEBUG)
			Log.d(TAG, "LocalFile writeFile()");

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// SD-карта не доступна
			// пишем во внут. память
			if (fileName == null || fileName.length() == 0)
				fileName = newFilename(false);
			return writeFileInternalStorage(scaledData, ctx, fileName);

		} else {
			if (fileName == null || fileName.length() == 0)
				fileName = newFilename(true);
			// пишем на SD-карту
			return writeFileSD(scaledData, ctx, fileName);
		}

	}

	static public String writeFileInternalStorage(byte[] Date, Context ctx,
			String sNewFilename) {
		try {

			/*
			 * FileOutputStream fOut = ctx.openFileOutput(Filename,
			 * ctx.MODE_PRIVATE); BufferedWriter bw = new BufferedWriter(new
			 * OutputStreamWriter(fOut));
			 * 
			 * bw.write(Date); bw.close();
			 */
			
			/*
			FileOutputStream fOut = ctx.openFileOutput(sNewFilename,
					ctx.MODE_PRIVATE);
			fOut.write(Date);
			fOut.close();
			return sNewFilename;
			*/

			
			ContextWrapper cw = new ContextWrapper(ctx);
			File directory = cw.getCacheDir();						
			
			FileOutputStream fos = new FileOutputStream(directory.getAbsolutePath()+"/"+sNewFilename);
			fos.write(Date);
			fos.close();
			
			return sNewFilename;
					
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public String writeFileSD(byte[] Date, Context ctx,
			String sNewFilename) {

		String sdPath = appDir();

		File sdFile = new File(sdPath, sNewFilename);
		try {
			// открываем поток для записи
			// BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
			// BufferedWriter bw = new BufferedWriter(new
			// FileOutputStream(sdFile));

			FileOutputStream fos = new FileOutputStream(sdFile);
			fos.write(Date);
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return sNewFilename;
	}

}
