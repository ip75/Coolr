package ru.cocsosoft;

import java.util.Enumeration;
import java.util.Hashtable;

public class Profile extends Hashtable<Long, LocalBox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4303405147202013066L;
	private TBDB dbTB;

	public Profile(TBDB db) throws Exception {
		this.dbTB = db;
		if (dbTB == null)
			throw new Exception("No database");
	}

	public LocalBox FindLocalBoxById(String id) {

		Enumeration<Long> dates = this.keys();
		LocalBox box = null;

		while (dates.hasMoreElements()) {
			Long date = dates.nextElement();
			box = get(date);
			if (box.id.compareTo(id) == 0)
				break;
		}

		return box;
	}
}
