public class QuickSort
{
	String category;
	public QuickSort(String c) {
		this.category = c;
	}

	int compareByArtist(Track t1, Track t2) {
		return t1.getArtist().compareTo(t2.getArtist()); 
	}

	int compareByTitle(Track t1, Track t2) {
		return t1.getTitle().compareTo(t2.getTitle()); 
	}
	
	int compareByDuration(Track o1, Track o2) {
		if (o2.getDuration() < o1.getDuration()) {
			return 1;
		}
		else if(o2.getDuration() > o1.getDuration()) {
			return -1;
		}
		else {
			return 0;
		}
	}

	int compare(Track t1, Track t2) {
		if (category.equals("artist")) {
			return compareByArtist(t1, t2);
		}
		else if (category.equals("title")) {
			return compareByTitle(t1, t2);
		}
		else {
			return compareByDuration(t1, t2);
		}
		
	}
	
	private int partition(Track array[], int start, int end)
	{
		Track pivotElement = array[end];
		int i = (start-1); 
		for (int j=start; j<end; j++)
		{
			if (compare(array[j],pivotElement) <=0)
			{
				i++;
				Track temp = array[i];
				array[i] = array[j];
				array[j] = temp;
			}
		}

		Track temp = array[i+1];
		array[i+1] = array[end];
		array[end] = temp;
		return i+1;
	}


	public void sort(Track array[], int start, int end)
	{
		if (start < end)
		{
			int pi = partition(array, start, end);
			sort(array, start, pi-1);
			sort(array, pi+1, end);
		}
	}
}

