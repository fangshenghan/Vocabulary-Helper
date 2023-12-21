package VocabularyHelper;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.R;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import VocabularyHelper.Helper.Utils;

public class Main {
	
	public static ExecutorService es = Executors.newCachedThreadPool();
	public static Scanner sc = new Scanner(System.in);
	public static String libraryPath = "/lib/";
	public static String exportPath = "/lib/output/";
	public static List<String> suggestedList = new ArrayList<>();
	
	public static void main(String[] args) throws Exception {
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1);
		if(args.length == 0) {
			if(path.endsWith(".jar")) {
				Runtime.getRuntime().exec("cmd /c start cmd /k java -jar " + path + " run");
				System.exit(0);
			}else {
				libraryPath = System.getProperty("user.home") + "/Desktop/lib/";
				exportPath = System.getProperty("user.home") + "/Desktop/lib/output/";
			}
		}else {
			libraryPath = path.substring(0, path.lastIndexOf("/")) + "/lib/";
			exportPath = path.substring(0, path.lastIndexOf("/")) + "/lib/output/";
		}
		
		//Utils.createWordsPlainText("C:\\Users\\fangs\\Desktop\\c01-grammar.txt", "C:\\Users\\fangs\\Desktop\\c01-words.txt", 10);
		
		// Initialize
		AnsiConsole.systemInstall();
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Welcome to VocabularyHelper [By 鲨鱼君Sharky]").reset());
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Initializing library...").reset());
		Utils.loadWords();
		
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Please enter the starting page: ").reset());
		int startPage = Integer.valueOf(sc.nextLine());
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Please enter the ending page: ").reset());
		int endPage = Integer.valueOf(sc.nextLine());
		
		// Suggest Words
		int suggested = 0;
		System.out.print(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Suggested Words: "));
		for(String wd : Utils.getWords(startPage, endPage)) {
			if(!suggestedList.contains(wd)) {
				System.out.print(Ansi.ansi().fgBright(Ansi.Color.WHITE).a(wd) + ", ");
				suggestedList.add(wd);
				suggested++;
				if(suggested == 10) break;
			}
		}
		
		// Input Words
		System.out.println();
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Please enter your choice of words: ").reset());
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Tip: Press 'Enter' for more suggestions!").reset());
		
		String choices = "";
		while(true) {
			choices = sc.nextLine().replace(" ", "");
			if(choices.length() > 0) {
				break;
			}
			suggested = 0;
			System.out.print(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Suggested Words: "));
			for(String wd : Utils.getWords(startPage, endPage)) {
				if(!suggestedList.contains(wd)) {
					System.out.print(Ansi.ansi().fgBright(Ansi.Color.WHITE).a(wd) + ", ");
					suggestedList.add(wd);
					suggested++;
					if(suggested == 10) break;
				}
			}
			System.out.println();
			System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("Please enter your choice of words: ").reset());
		}
		
		System.out.println("Fetching data from: https://www.merriam-webster.com/");
		System.out.println();
		
		// Prepare HTML File
		String html = "<style>div{font-size: 15.99px; font-family: \"Times New Roman\"}</style>\n";
		
		// Prepare Word Document
		WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart document = wordPackage.getMainDocumentPart();
		
		// Failed Word List
		List<String> failedWords = new ArrayList<>();
		
		// Start Processing
		for(String word : choices.split(",")) {
			try {
				int page = Utils.getWordPage(word, startPage, endPage);
				
				Document doc = Jsoup.parse(new URL("https://www.merriam-webster.com/dictionary/" + word), 10000);
				
				// Page & Word
				String part = doc.getElementsByClass("parts-of-speech").get(0).getAllElements().get(0).text();
				String pron = doc.getElementsByClass("prons-entry-list-item").get(0).text();
				
				if(part.equals("preposition")) {
					part = "prep.";
				}else {
					part = part.substring(0, 1) + ".";
				}
				
				System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("(p." + page + ") " + word + " \\ " + pron + " \\"));
				html += "<div>(p." + page + ") " + word + " \\ " + pron + " \\</div>" + "\n";
				
				document.addObject(Utils.createParagraph(
						Utils.createStyledText("(p." + page + ") ", 12, false),
						Utils.createStyledText(word, 12, true),
						Utils.createStyledText(" \\ " + pron + " \\", 12, false)
						));
				
				// Meanings
				int cnt = 0;
				Element vg = doc.getElementsByClass("vg").get(0);
				for(Element e : vg.getElementsByClass("dtText")) {
					if(!e.text().startsWith(": ")) {
						continue;
					}
					cnt++;
					if(cnt == 1) {
						html += "<div><strong>[" + part + "] " + e.text().substring(2) + "</strong></div>" + "\n";
						document.addObject(Utils.createParagraph(
								Utils.createStyledText("[" + part + "] " + e.text().substring(2), 12, true)
								));
					}else {
						html += "<div>[" + part + "] " + e.text().substring(2) + "</div>\n";
						document.addObject(Utils.createParagraph(
								Utils.createStyledText("[" + part + "] " + e.text().substring(2), 12, false)
								));
					}
					System.out.println("[" + part + "] " + e.text().substring(2));
					if(cnt == 3) break;
				}
				
				// Synonyms
				if(doc.getElementsByClass("synonyms-antonyms-grid-list").size() > 0) {
					cnt = 0;
					System.out.print("Synonyms:");
					html += "<div><strong>Synonyms</strong>:";
					List<R> texts = new ArrayList<R>();
					texts.add(Utils.createStyledText("Synonyms:", 12, true));
					
					for(Element e : doc.getElementsByClass("synonyms-antonyms-grid-list").get(0).getAllElements()) {
						if(!e.parent().toString().contains("<ul")) {
							System.out.print(" " + e.text());
							html += " " + e.text();
							texts.add(Utils.createStyledText(" " + e.text(), 12, false));
							cnt++;
							if(cnt == 5) break;
						}
					}
					document.addObject(Utils.createParagraph(texts));
					
					System.out.println();
					html += "\n</div>";
				}
				
				// Sentences
				if(doc.getElementsByClass("in-sentences").size() > 0) {
					cnt = 0;
					for(Element e : doc.getElementsByClass("in-sentences").get(0).getElementsByClass("d-block")) {
						System.out.println("    " + e.text());
						document.addObject(Utils.createParagraph(
								Utils.createStyledText("    " + e.text(), 12, false)
								));
						
						html += "<div>    " + e.text() + "</div>\n";
						cnt++;
						if(cnt == 2) break;
					}
				}
				
				// End
				System.out.println();
				html += "<div>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</div>";
				document.addObject(Utils.createParagraph(
						Utils.createStyledText("", 12, false)
						));
			}catch(Exception ex) {
				System.out.println(Ansi.ansi().fgBright(Ansi.Color.RED).a("Processing Failed: " + word).reset());
				System.out.println(Ansi.ansi().fgBright(Ansi.Color.RED).a(ex.getMessage()).reset());
				failedWords.add(word);
			}
		}
		
		// Print Failed Words
		if(failedWords.size() > 0) {
			System.out.println(Ansi.ansi().fgBright(Ansi.Color.YELLOW).a("Failed Words: "));
			for(String word : failedWords) {
				System.out.print(Ansi.ansi().fgBright(Ansi.Color.YELLOW).a(word + ", "));
			}
			System.out.println();
		}
		
		// Export Files
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Exporting VocabularyNotebook.docx...").reset());
		try {
			wordPackage.save(new File(Main.exportPath + "VocabularyNotebook.docx"));
		}catch(Exception ex) {
			if(ex.toString().contains("另一个程序正在使用此文件")) {
				System.out.println(Ansi.ansi().fgBright(Ansi.Color.RED).a("Exporting to VocabularyNotebook.docx failed! (Another program is using this file)").reset());
			}else {
				ex.printStackTrace();
			}
		}
		
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Exporting VocabularyNotebook.html...").reset());
		Utils.write(html, new File(Main.exportPath + "VocabularyNotebook.html"));
		
		// Finished
		System.out.println(Ansi.ansi().fgBright(Ansi.Color.GREEN).a("Finished!").reset());
	}
	
}
