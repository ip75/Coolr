package ru.cocsosoft;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Vote")
public class Vote extends ParseObject
{

	public Vote()
	{
		//версия объекта март 2014 = 1
		final int CurrentVer = 1;
				
		//для всех объектов будем вести версионность
		if (getInt("ver")==0)
		{
			put("ver", CurrentVer);
		}
	}
	
	public boolean makeVote( 
			Box item,   //  ссылка на размещенный объект с картинками 
			ParseUser user,   // Тот юзер, кто проголосовал 
			int iImageId )  // для заполнения полей Img1 и Img2
	{
		// заполняем объект Vote
		put("box", item);
		put("user", user);
		put("Img" + String.valueOf(iImageId) , iImageId);

		// 
		item.updateRank(iImageId);
		return true;
	}
	
}
