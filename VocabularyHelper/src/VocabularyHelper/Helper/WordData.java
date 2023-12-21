package VocabularyHelper.Helper;

public class WordData {
	
	public String word;
	public int times;
	
	public WordData(String word, int times) {
		this.word = word;
		this.times = times;
	}
	
	public WordData addTimes(int times) {
		this.times += times;
		return this;
	}

}
