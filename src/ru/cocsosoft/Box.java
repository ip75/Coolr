package ru.cocsosoft;

import java.util.UUID;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Box")
public class Box extends ParseObject {

	final int CurrentVer = 1;

	public void Init()
	{
		// для всех объектов будем вести версионность
		put("ver", CurrentVer);

	}
	
	// Тот юзер, кто сфоткал 2 фотки и запостил их в систему
	public void setAuthor(ParseUser user) {
		put("author", user);
		Init();
	}
	
	public void setBoxID(String boxID) {
		put("boxID", boxID);
	}


	// возвращает внут. идентификатор коробки
	public String getBoxID() {
		return getString("boxID");
	}

    public int getVote(int numOfVote) {
        return getInt("Votes" + String.valueOf(numOfVote));
    }


	public void setImage1(ParseFile img) {
		if (img != null)
			put("Img1", img);
	}

	public void setImage2(ParseFile img) {
		if (img != null)
			put("Img2", img);
	}

	public void updateRank(int iImageId) {
		String imgFieldName = "Votes" + String.valueOf(iImageId);

		put("Rank", getInt("Rank") + 1);
		put(imgFieldName, getInt(imgFieldName) + 1);
	}

	public ParseFile getImage1() {
		return getParseFile("Img1");
	}

	public ParseFile getImage2() {
		return getParseFile("Img2");
	}

	public static ParseQuery<Box> getQuery() {
		return ParseQuery.getQuery(Box.class);
	}

}
