package models.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by samuel on 4/7/15.
 */
public class ArrayHelper {
	public static ArrayList<Long> removeDuplicates(ArrayList<Long> a_master) {
		ArrayList<Long> a = new ArrayList<Long>(a_master);
		Set<Long> set = new HashSet<Long>();
		List<Long> newList = new ArrayList<Long>();
		for (Iterator<Long> iter = a.iterator(); iter.hasNext();) {
			Long element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		a.clear();
		a.addAll(newList);
		return a;
	}
	
	public static boolean stringContainsItemFromList(String inputString, String[] items)
	{
	    for(int i =0; i < items.length; i++)
	    {
	        if(inputString.contains(items[i]))
	        {
	            return true;
	        }
	    }
	    return false;
	}

}
