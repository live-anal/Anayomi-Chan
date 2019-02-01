package pkg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import main.com.j5.connect.Content;
import main.com.j5.connect.J5ch;
import main.com.j5.connect.ResultSet;
import main.com.j5.connect.method.SuperAnalizer;
import main.com.j5.exception.ChinkoException;
import main.com.j5.exception.MankoException;
import main.com.j5.exception.UnkoException;
import pkg.runnable.AnayomiRunnable;
import pkg.runnable.KeepRunnable;
import pkg.runnable.SubyomiRunnable;
import pkg.runnable.SuperRunnable;
import pkg.util.LinkedProperties;
import pkg.util.Voice;
import pkg.util.VoiceListener;

public class Main implements VoiceListener{
	public static final String RESOURCE_PATH = "res/";
	public static final String BY_MAIN_PATH = RESOURCE_PATH + "BouyomiChan/main/";
	public static final String BY_INLINE_PATH = RESOURCE_PATH + "BouyomiChan/inline/";
	public static final String PROP_PATH = RESOURCE_PATH + "properties.conf";
	public static final String NGLIST_PATH = RESOURCE_PATH + "nglist.txt";
	public static final String NGTHREAD_PATH = RESOURCE_PATH + "ngthread.txt";

	private static LinkedProperties prop;
	private static List<String> nglist;
	private static List<String> ngthread;

	private static Voice main;
	private static Voice inline;
	private static J5ch j5;

	private static BufferedReader input;
	private static List<Thread> threads;
	private static boolean run;

	private static boolean inline_flag=false;

	public static void main(String[] args) throws FileNotFoundException, IOException, ChinkoException, UnkoException {
		loadProperties();
		loadNGList();
		loadNGThread();
		showConfig();

		initializeMain();

		initializeVoiceMain();

		if(prop.getBool("INLINE")) {
			inline_flag = true;
			initializeVoiceInline();
		}

		initializeJ5Lib();

		startVoiceMain();
		if(inline_flag) {
			startVoiceInline();
		}

		startJ5Lib();

		new Main();
	}

	public static void loadProperties() {
		prop = new LinkedProperties(PROP_PATH);
	}

	public static void loadNGList() {
		nglist = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NGLIST_PATH)), "UTF-8"))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				nglist.add(line);
			}

			if(!prop.getBool("KEEP_POST_YOMIAGE")) {
				nglist.add(prop.getString("KEEP_POST_MESSAGE"));
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadNGThread() {
		ngthread = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NGTHREAD_PATH)), "UTF-8"))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				ngthread.add(line);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void showConfig() {
		System.out.println(prop.toString());

		System.out.println("========NGList========");
		for (String line : nglist) {
			System.out.println(line);
		}

		System.out.println("========NGThread========");
		for (String line : ngthread) {
			System.out.println(line);
		}
		System.out.println("========================");
	}

	public static void initializeMain() {
		input = new BufferedReader(new InputStreamReader(System.in));
		threads = new ArrayList<>();
		run = false;
	}

	public static void initializeVoiceMain() {
		main = new Voice(prop.getInt("PORT"));

		// BouyomiChan.settingにポートを設定
		File file = new File(BY_MAIN_PATH + "BouyomiChan.setting");
		List<String> list = new ArrayList<>();

		// 設定ファイル読み込み
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				list.add(line);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}

		// 7行目と9行目(0から数えて)を更新
		list.set(6, "  <PortNumber>" + prop.getInt("PORT") + "</PortNumber>");
		list.set(8, "  <PortNumberHttp>"+ prop.getInt("HTTP_PORT") +"</PortNumberHttp>");

		// 元のファイルを退避
		file.renameTo(new File(BY_MAIN_PATH + "BouyomiChan.setting_bak"));

		// ファイルを削除して新規作成（中身初期化）
		file = new File(BY_MAIN_PATH + "BouyomiChan.setting");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 設定ファイルに書き込み
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			for (String s : list) {
				bw.write(s + "\r\n");
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initializeVoiceInline() {
		inline = new Voice(prop.getInt("INLINE_PORT"));

		// BouyomiChan.settingにポートを設定
		File file = new File(BY_INLINE_PATH + "BouyomiChan.setting");
		List<String> list = new ArrayList<>();

		// 設定ファイル読み込み
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				list.add(line);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}

		// 9行目と11行目(0から数えて)を更新
		list.set(8, "  <PortNumber>" + prop.getInt("INLINE_PORT") + "</PortNumber>");
		list.set(10, "  <PortNumberHttp>"+ prop.getInt("INLINE_HTTP_PORT") +"</PortNumberHttp>");

		// 元のファイルを退避
		file.renameTo(new File(BY_INLINE_PATH + "BouyomiChan.setting_bak"));

		// ファイルを削除して新規作成（中身初期化）
		file = new File(BY_INLINE_PATH + "BouyomiChan.setting");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 設定ファイルに書き込み
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
			for (String s : list) {
				bw.write(s + "\r\n");
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initializeJ5Lib() throws ChinkoException {
		j5 = new J5ch(prop.getString("HOST"), prop.getString("BBS"));
		j5.set5chAPI(prop.getString("APPKEY"), prop.getString("HMKEY"), prop.getString("USERAGENT"), prop.getString("X_2ch_UA"));
		j5.setPostUA(prop.getString("KEEP_POST_USERAGENT"));
		j5.setAnal(new SuperAnalizer());
	}

	public static void startVoiceMain() {
		try {
			main.run(BY_MAIN_PATH + "BouyomiChan.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void startVoiceInline() {
		try {
			inline.run(BY_INLINE_PATH + "BouyomiChan.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void startJ5Lib() throws ChinkoException {
		j5.auth5chAPI();
	}

	public Main() throws UnkoException, IOException {
		boolean end = false;

		System.out.println("put ?(Help) or Command");

		while (!end) {
			if (!run)
				System.out.print("> ");
			String in = input.readLine();

			if (in != null) {
				switch (in) {
				case "get subject":
				case "g sub":
				case "g s":
				case "gs":
					getSubject();
					break;

				case "stop":
					stop();
					run = false;
					break;

				case "help":
				case "?":
					help();
					break;

				case "exit":
					end = true;
					break;

				case "speech subject":
				case "s sub":
				case "s s":
				case "ss":
					if (!run) {
						run = true;
						startSubyomi();
					} else {
						System.out.println("読み上げ中です。先にstopコマンドで読み上げを停止してください。");
					}
					break;

				default:
					if (in.matches("[0-9]{10}")) {
						if (!run) {
							run = true;
							startAnayomi(in);
						} else {
							runnedMessage();
						}

					}else if(in.matches("new subject .+")) {
						createThread("new subject ", in);

					}else if(in.matches("new sub .+")) {
						createThread("new sub ", in);

					}else if(in.matches("n s .+")) {
						createThread("n s ", in);

					}else if(in.matches("ns .+")) {
						createThread("ns ", in);

					}else {
						getSubject();
					}
				}
			} else {
				end = true;
			}
		}

		for (Thread t : threads) t.interrupt();
		main.destroy();
		if(inline_flag)
			inline.destroy();
	}

	public void getSubject() throws UnkoException {
		System.out.println(j5.getSubject().toStringBody());
	}

	public void stop() {
		threads.parallelStream().forEach(t -> {
			while(true) {
				if(!t.isInterrupted()) {
					t.interrupt();
					break;
				}
			}
		});

		threads = new ArrayList<>();
	}

	public void help() {
		String str = "";

		str += "Help\n";
		str += "\n";
		str += "(1) スレタイ一覧の取得コマンド\n";
		str += "\t> get subject\n";
		str += "\t> g sub\n";
		str += "\t> g s\n";
		str += "\t> gs\n";
		str += "(2) 読み上げ停止コマンド\n";
		str += "\t> stop\n";
		str += "(3) ヘルプ表示コマンド\n";
		str += "\t> help\n";
		str += "\t> ?\n";
		str += "(4) スレの読み上げ開始コマンド\n";
		str += "\t> スレ番10桁([0-9]{10})\n";
		str += "(5) 終了コマンド\n";
		str += "\t> exit\n";
		str += "(6) スレ一覧の読み上げ\n";
		str += "\t> speech subject\n";
		str += "\t> s sub\n";
		str += "\t> s s\n";
		str += "\t> ss\n";
		str += "(7) スレ作成と読み上げ\n";
		str += "\t> new subject {title} {msg}\n";
		str += "\t> new sub {title} {msg}\n";
		str += "\t> n s {title} {msg}\n";
		str += "\t> ns {title} {msg}\n";
		str += "\n";

		System.out.println(str);
	}

	public void startSubyomi() {
		SuperRunnable r = new SubyomiRunnable(j5, prop, ngthread, nglist, prop.getInt("NG_NUM"), this);

		Thread t = new Thread(r);
		t.start();

		threads.add(t);
	}

	public void startAnayomi(String key) {
		SuperRunnable r = new AnayomiRunnable(j5, prop, nglist, prop.getInt("NG_NUM"), this);
		r.setKey(key);

		Thread t = new Thread(r);
		t.start();

		threads.add(t);

		if(prop.getBool("KEEP_THREAD")) {
			startKeepThread(key);
		}
	}

	public void startKeepThread(String key) {
		SuperRunnable r = new KeepRunnable(j5, prop, prop.getInt("NG_NUM"), this);
		r.setKey(key);

		Thread t = new Thread(r);
		t.start();

		threads.add(t);
	}

	public void createThread(String cmd, String text) {
		String[] data = subcmd(cmd, text);

		if(!run) {
			run = createThread(data);

		}else {
			runnedMessage();
		}
	}

	private boolean createThread(String[] data) {
		boolean f = false;

		try {
			ResultSet rs = j5.post(true, data[0], "", "", data[1]);
			System.out.println(rs.toStringBody());

			if(rs.get(2).contains("書きこみました。")) {

				Thread.sleep(1000);
				rs = j5.getSubject();

				for(String s : rs) {
					if(s.contains(data[0])) {
						String key = s.substring(0,10);

						startAnayomi(key);

						f = true;
						break;
					}
				}
			}

		} catch (UnkoException e) {
			e.printStackTrace();
		} catch (MankoException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return f;
	}

	private String[] subcmd(String cmd, String msg) {
		return msg.substring(cmd.length(), msg.length()).split(" ", 2);
	}

	private void runnedMessage() {
		System.out.println("読み上げ中です。先にstopコマンドで読み上げを停止してください。");
	}

	@Override
	public boolean addText(String msg) {
		main.addText(msg);
		if(inline_flag) inline.addText(msg);

		return true;
	}

	public boolean addText(String str, String msg) {
		String buf = msg;
		boolean f = true;

		if(buf.split("\n").length>prop.getInt("NG_LINE")) {
			buf = "長文のため以下省略。";
			f = false;
		}

		addText(str+buf);

		return f;
	}

	@Override
	public boolean addText(Content c, int num) {
		String buf = c.getMessage();
		boolean f = true;

		if(buf.split("\n").length>prop.getInt("NG_LINE")) {
			buf = "長文のため以下省略。";
			f = false;
		}

		String yomiage = "レス"+num+"、"+buf;
		addText(yomiage);

		String from = c.getFrom();
		from = from.substring(from.indexOf(">") + 1, from.lastIndexOf("<"));

		String str = "";
		str += num + " " + from + "[" + c.getMail() + "] " + c.getDateString();
		str += (c.getId() != null && !c.getId().isEmpty()) ? "ID:" + c.getId() : "";
		str += "\n";
		str += c.getMessage();
		str += "\n";

		System.out.println(str);

		return f;
	}

	@Override
	public void down() {
		String str = "スレが落ちたようです。読み上げを終了します。\n";
		addText(str);

		run = false;
		stop();
	}
}
