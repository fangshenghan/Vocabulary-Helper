package VocabularyHelper.Helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import VocabularyHelper.Main;

public class Utils {
	
	public static HashMap<String, Integer> wordMap = new HashMap<>();
	public static Gson gson = new Gson();
	public static JsonObject words;
	
	public static String read(String path, String charset) {
		File file = new File(path);
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(new FileInputStream(file), charset);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer res = new StringBuffer();
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					res.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return res.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	public static boolean write(String cont, File dist) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dist), "UTF-8");
			BufferedWriter writer = new BufferedWriter(osw);
			writer.write(cont);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void createWords() {
		Utils.loadWordFrequency();
		HashMap<String, WordData> map = new HashMap<>();
		
		JsonObject json = new JsonObject();
		JsonArray words = new JsonArray();
		String[] lines = Utils.read(Main.libraryPath + "BeingMortal.txt", "UTF-8").split("\n");
		List<String> added = new ArrayList<>();
		
		for(String l : lines) {
			if(Utils.isNumber(l)) {
				json.add(l, words);
				words = new JsonArray();
			}else {
				for(String w1 : l.split(" ")) {
					for(String w2 : w1.split("—")) {
						for(String word : w2.split("-")) {
							word = Utils.formatWord(word);
							if(word != "") {
								if(word.length() > 2 && !added.contains(word)) {
									words.add(word);
									added.add(word);
								}
								//if(wordMap.containsKey(word.toLowerCase())) {
								//	map.put(word, new WordData(word, wordMap.get(word.toLowerCase())));
								//}else {
								//	System.out.println("Unknown word: " + word);
								//}
							}
						}
					}
				}
			}
		}
		
		Utils.write(new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(json), new File(Main.libraryPath + "Words.json"));
		
		/*List<WordData> frequencyList = new ArrayList<>();
		for(WordData wd : map.values()) {
			frequencyList.add(wd);
		}
		frequencyList.sort(new Comparator<WordData>() {
			@Override
			public int compare(WordData o1, WordData o2) {
				return o2.times - o1.times;
			}
		});
		
		String freq = "";
		for(WordData wd : frequencyList) {
			freq += wd.word + " " + wd.times + "\n";
		}
		
		Utils.write(freq, new File(Main.libraryPath + "WordFrequency.txt"));*/
		
		System.exit(0);
	}
	
	public static void createWordsPlainText(String path, String output, int keepPercentage) {
		Utils.loadAllWordFrequency();
		
		String[] lines = Utils.read(path, "UTF-8").split("\n");
		
		List<String> words = new ArrayList<>();
		for(String l : lines) {
			for(String w1 : l.split(" ")) {
				for(String w2 : w1.split("—")) {
					for(String word : w2.split("-")) {
						word = Utils.formatWord(word);
						if(word != "") {
							if(word.length() > 2 && !words.contains(word) && wordMap.containsKey(word.toLowerCase())) {
								words.add(word);
							}
						}
					}
				}
			}
		}
		
		words.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return wordMap.get(o1.toLowerCase()) - wordMap.get(o2.toLowerCase());
			}
		});
		
		JsonArray array = new JsonArray();
		String result = "";
		for(int i = 0;i < words.size() / (100 / keepPercentage);i++) {
			array.add(words.get(i));
			result += words.get(i) + ", ";
		}
		//Utils.write(new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(array), new File(output));
		Utils.write(result.substring(0, result.length() - 2), new File(output));
		
		System.out.println("Done");
		System.exit(0);
	}
	
	public static String formatWord(String s) {
		s = s.replace("'s", "").replace("'d", "");
		String word = "";
		for(char c : s.toCharArray()) {
			if(c == '%' || c == '.') {
				return "";
			}
			if(Character.isAlphabetic(c) || c == '\'') {
				word += c;
			}
		}
		if(word.length() > 1) {
			return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
		}
		return word;
	}
	
	public static void loadWords() {
		Utils.words = Utils.gson.fromJson(Utils.read(Main.libraryPath + "Words.json", "UTF-8"), JsonObject.class);
		Utils.loadWordFrequency();
	}
	
	public static void loadAllWordFrequency() {
		String[] lines = Utils.read(Main.libraryPath + "EnglishWordFrequency.txt", "UTF-8").split("\n");
		for(String l : lines) {
			String[] split = l.split(" ");
			wordMap.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
		}
	}
	
	public static void loadWordFrequency() {
		String[] lines = Utils.read(Main.libraryPath + "WordFrequency.txt", "UTF-8").split("\n");
		for(String l : lines) {
			String[] split = l.split(" ");
			wordMap.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
		}
	}
	
	public static List<String> getWords(int startPage, int endPage) {
		if(Utils.words == null) {
			Utils.loadWords();
		}
		
		List<String> added = new ArrayList<>();
		for(int i = startPage;i <= endPage;i++) {
			if(words.has(i + "")) {
				for(JsonElement je : words.get(i + "").getAsJsonArray()) {
					String word = je.getAsString();
					if(!added.contains(word)) {
						if(wordMap.containsKey(word.toLowerCase())) {
							added.add(word);
						}
					}
				}
			}
		}
		
		added.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return wordMap.get(o1.toLowerCase()) - wordMap.get(o2.toLowerCase());
			}
		});
		
		List<String> list = new ArrayList<>();
		for(int i = 0;i < added.size() / 10;i++) {
			list.add(added.get(i));
		}
		
		Collections.shuffle(list);
		
		return list;
	}
	
	public static int getWordPage(String word, int startPage, int endPage) {
		for(int i = startPage;i <= endPage;i++) {
			if(words.has(i + "")) {
				for(JsonElement je : words.get(i + "").getAsJsonArray()) {
					if(je.getAsString().toLowerCase().equals(word.toLowerCase())) {
						return i;
					}
				}
			}
		}
		return 0;
	}
	
	public static boolean isNumber(String s) {
		try {
			Integer.valueOf(s);
			return true;
		}catch(Exception ex) {
			return false;
		}
	}
	
	public static P createParagraph(List<R> rs) {
		ObjectFactory factory = Context.getWmlObjectFactory();
		P para = factory.createP();
		
		PPr ppr = factory.createPPr();
		Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(new BigInteger("1"));
        spacing.setBefore(new BigInteger("1"));
		ppr.setSpacing(spacing);
		
		para.setPPr(ppr);
		
		for(R r : rs) {
			para.getContent().add(r);
		}
		return para;
	}
	
	public static P createParagraph(R... rs) {
		ObjectFactory factory = Context.getWmlObjectFactory();
		P para = factory.createP();
		
		PPr ppr = factory.createPPr();
		Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(new BigInteger("1"));
        spacing.setBefore(new BigInteger("1"));
		ppr.setSpacing(spacing);
		
		para.setPPr(ppr);
		
		for(R r : rs) {
			para.getContent().add(r);
		}
		return para;
	}
	
	public static R createStyledText(String text, int size, boolean bold) {
		ObjectFactory factory = Context.getWmlObjectFactory();
		Text t = factory.createText();
		t.setValue(text);
		t.setSpace("preserve");
		
		R r = factory.createR();
		r.getContent().add(t);
		
		RPr rpr = factory.createRPr();
		if(bold) {
			rpr.setB(new BooleanDefaultTrue());
		}
		
		HpsMeasure hm = new HpsMeasure();
		hm.setVal(new BigInteger(size * 2 + ""));
		rpr.setSz(hm);
		
        RFonts fonts = factory.createRFonts();
        fonts.setAscii("Times New Roman");
        fonts.setHAnsi("Times New Roman");
		rpr.setRFonts(fonts);
		
		r.setRPr(rpr);
		
		return r;
	}
	
}
