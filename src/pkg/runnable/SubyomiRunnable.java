package pkg.runnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.com.j5.connect.J5ch;
import main.com.j5.connect.ResultSet;
import main.com.j5.exception.UnkoException;
import pkg.util.LinkedProperties;
import pkg.util.VoiceListener;

public class SubyomiRunnable extends SuperRunnable{
	Map<String, ResultSet> modifylist = new HashMap<>();

	public SubyomiRunnable(J5ch j5, LinkedProperties prop, List<String> ngthread, List<String> nglist, int ng_num, VoiceListener v) {
		super(v, ng_num);
		setJ5ch(j5);
		setProperties(prop);
		setNGThread(ngthread);
		setNGList(nglist);
	}

	@Override
	public void run() {
		ResultSet before;
		boolean subyomi_autong 	= prop.getBool("SUBYOMI_AUTONG");
		boolean subyomi_title	= prop.getBool("SUBYOMI_TITLE");
		boolean subyomi_res		= prop.getBool("SUBYOMI_RES");
		int pausetime				= prop.getInt("PAUSE_TIME");

		// 初回取得
		while (true) {
			try {
				ResultSet rs = j5.getSubject();

				before = subNGThread(rs);

				for (String line : before) {
					ThreadContent th = new ThreadContent(line);
					ResultSet r = j5.get(th.key);

					if(subyomi_autong) {
						if(!checkNG(r.getContent(0).getMessage())){
							modifylist.put(th.key, r);
						}else {
							ngthread.add(th.title);
						}
					}else {
						modifylist.put(th.key, r);
					}
				}

				System.out.println(rs);

				break;
			} catch (UnkoException e) {
				continue;
			}
		}

		// 逐次取得
		boolean run = true;
		while (run && !Thread.interrupted()) {
			try {
				// 差分取得
				ResultSet after = j5.getSubject(before.lastmodify);

				// 更新あり
				if (after.code == 200) {
					ResultSet rs;

					// スレ増分
					rs = subNGThread(after.getNewSubject(before));

					if (!rs.isEmpty()) {
						addText("スレが増えたよ。");

						// スレタイ解析
						for (String s : rs) {
							ThreadContent th = new ThreadContent(s);

							// スレ取得
							ResultSet r = j5.get(th.key);

							// autongであればng.add()付き処理
							if(subyomi_autong) {
								if(!checkNG(r.getContent(0).getMessage())){
									if (subyomi_title)
										addText(th.title + "\n");
									if (subyomi_res) {
										addText("レス1 ", r.getContent(0).getMessage() + "\n");
										modifylist.put(th.key, r);
									}
								} else {
									ngthread.add(th.title);
								}

							// autongでなければ判定なし
							}else {
								if (subyomi_title)
									addText(th.title + "\n");
								if (subyomi_res) {
									addText("レス1 ", r.getContent(0).getMessage() + "\n");
									modifylist.put(th.key, r);
								}
							}

							addText("\n\n");
						}
					}

					// スレ更新
					rs = subNGThread(after.getUpdateSubject(before));
					if (!rs.isEmpty()) {
						addText("更新されたよ。");

						// スレタイ解析
						for (String s : rs) {
							ThreadContent th = new ThreadContent(s);

							if (subyomi_title)
								addText(th.title + "\n");
							if (subyomi_res) {
								ResultSet b = modifylist.get(th.key);
								ResultSet r = (b != null) ? j5.get(th.key, b.lastmodify, b.bytes) : j5.get(th.key);

								if (b != null) {
									for (int i = 0; i < r.getContent().size(); i++) {
										if (i == 0)
											addText("に、", r.getContent(i).getMessage());
										else
											addText("と、", r.getContent(i).getMessage());
									}

									b.lastmodify = r.lastmodify;
									b.bytes += r.bytes;

								} else {
									addText("に、", r.getContent(r.size() - 1).getMessage());
									b = r;
								}

								modifylist.replace(th.key, b);
							}

							addText("\n\n");
						}
					}

					// スレ減分
					rs = subNGThread(before.getNewSubject(after));
					if (!rs.isEmpty()) {
						addText("スレが消えたよ。");
						for (String s : rs) {
							ThreadContent th = new ThreadContent(s);
							if (subyomi_title)
								addText(th.title + "\n\n");

							modifylist.remove(th.key);
						}
					}

					System.out.println(after.toStringBody());
					before = after;

					// 304以外の場合コード表示
				} else if (after.code != 304) {
					System.out.println(after.code);
				}

				Thread.sleep(pausetime);

			} catch (UnkoException e) {
				continue;
			} catch (InterruptedException e) {
				run = false;
			}
		}
	}

	public void addText(String string, String message) {
		super.addText(string, message);
	}

	public ResultSet subNGThread(ResultSet rs) {
		ResultSet buf = rs.copyHeader();

		for(String r : rs) {
			boolean f = false;

			ThreadContent c = new ThreadContent(r);
			for(String n : ngthread) {
				if(c.title.contains(n) || c.title.matches(n)) {
					f = true;
					break;
				}
			}

			if(!f) {
				buf.add(r);
			}
		}

		return buf;
	}

	public class ThreadContent {
		public String key;
		public String title;
		public String res;

		public ThreadContent(String s){
			key = s.substring(0,10);
			title = s.substring(s.indexOf(">")+1, s.lastIndexOf("(")).trim();
			res = s.substring(s.lastIndexOf("(")+1, s.lastIndexOf(")"));
		}
	}

}
