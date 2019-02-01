package pkg.runnable;

import java.util.Calendar;
import java.util.Date;

import main.com.j5.connect.J5ch;
import main.com.j5.connect.ResultSet;
import main.com.j5.exception.MankoException;
import main.com.j5.exception.UnkoException;
import pkg.util.LinkedProperties;
import pkg.util.VoiceListener;

public class KeepRunnable extends SuperRunnable {

	public KeepRunnable(J5ch j5, LinkedProperties prop, int ng_num, VoiceListener v) {
		super(v, ng_num);
		setJ5ch(j5);
		setProperties(prop);
	}

	@Override
	public void run() {
		int keep_thread_time = prop.getInt("KEEP_THREAD_TIME");
		int keep_thread_maxtime = prop.getInt("KEEP_THREAD_MAXTIME");
		int keep_thread_maxnum = prop.getInt("KEEP_THREAD_MAXNUM");
		int keep_thread_depth = prop.getInt("KEEP_THREAD_DEPTH");
		int keep_load_retry = prop.getInt("KEEP_RETRY");
		int keep_load_millis = prop.getInt("KEEP_RETRY_MILLIS");
		String keep_post_message = prop.getString("KEEP_POST_MESSAGE");

		boolean run = true;
		Calendar future = Calendar.getInstance();
		String thread_lastmodify = null;
		String lastmodify = null;
		int bytes = 0;

		// 開始時間を次の60-depth秒に設定
		Calendar nextload = Calendar.getInstance();
		nextload.setTime(new Date());
		nextload.set(Calendar.SECOND, 60 - keep_thread_depth);
		nextload.set(Calendar.MILLISECOND, 0);

		while(run) {
			boolean post = false;

			try {
				// 停止時間計算
				long next = nextload.getTimeInMillis() - new Date().getTime();

				// 過ぎていなければ時間分スリープ
				if(next > 0) {
					Thread.sleep(next);
				}

				// レス取得
				ResultSet res = new ResultSet();
				boolean res_f = false;
				for(int i=0; i<keep_load_retry && !res_f; i++) {
					try {
						if(lastmodify==null) {
							res = j5.get(key);
						}else {
							res = j5.get(key, lastmodify, bytes);
						}

						// 更新があれば最後の書き込み時間から3分後の時間と取得時間を保持
						if(res!=null && !res.isEmpty()) {
							future.setTime(res.getContent(res.getContent().size()-1).getDate());
							future.add(Calendar.MINUTE, keep_thread_time+1);
							future.set(Calendar.SECOND, 0);
							future.set(Calendar.MILLISECOND, 0);
							future.add(Calendar.SECOND, -keep_thread_depth);

							lastmodify = res.lastmodify;
							bytes += res.bytes;
						}

						res_f = true;
					} catch (UnkoException e) {
						e.printStackTrace();
						Thread.sleep(keep_load_millis);
					}
				}

				// スレ一覧取得
				ResultSet thread = new ResultSet();
				boolean th_f = false;
				for(int i=0; i<keep_load_retry && !th_f; i++) {
					try {
						if(thread_lastmodify==null) {
							thread = j5.getSubject(key);
						}else {
							thread = j5.getSubject(thread_lastmodify);
						}

						if(thread!=null && !thread.isEmpty()) {
							thread_lastmodify = thread.lastmodify;
						}

						th_f = true;
					} catch (UnkoException e) {
						e.printStackTrace();
						Thread.sleep(keep_load_millis);
					}
				}

				// レスかスレ一覧が取得できなければ終わり
				if(!res_f) {
					threadDown();
					throw new InterruptedException();
				}else if(!th_f) {
					notSubjects();
					throw new InterruptedException();
				}

				// (1) スレのインデックスがkeep_thread_maxnumを上回っているか？
				for(int i=0; i<thread.size(); i++) {
					String line = thread.get(i);

					if(!line.contains("924") && line.contains(key)) {
						if((i+1) >= keep_thread_maxnum) {
							post = true;
						}
						break;
					}
				}

				// (2) 最後の書き込みから3分経過してるか？
				if(!post && nextload.getTimeInMillis() - future.getTimeInMillis() >= 0) {
					post = true;
				}

				// (3) 更新が必要か？
				if(post) {
					boolean f = false;
					for(int i=0; i<keep_load_retry && !f; i++) {
						try {
							ResultSet rs = j5.post(key, "", "", keep_post_message);
							if(rs.get(2).contains("書きこみました")) {
								f = true;
							}else if(rs.get(6).contains("連投")) {
								j5.clearCookie();
								continue;
							}
						} catch (UnkoException | MankoException e) {
							e.printStackTrace();
							Thread.sleep(keep_load_millis);
						}
					}

					if(!f) {
						notPost();
						throw new InterruptedException();
					}
				}

				// 次の更新時間を設定
				nextload.add(Calendar.MINUTE, keep_thread_maxtime);

			}catch(InterruptedException e) {
				if(prop.getBool("KEEP_AUTOSTOP")) {
					run = false;
				}
			}
		}
	}

	private void threadDown() {
		System.out.println("3回試しましたがスレが取得できませんでした。");
		if(prop.getBool("KEEP_AUTOSTOP")) {
			System.out.println("保守を終了します。");
		}
	}

	private void notSubjects() {
		System.out.println("3回試しましたがスレ一覧が取得できませんでした。");
		if(prop.getBool("KEEP_AUTOSTOP")) {
			System.out.println("保守を終了します。");
		}
	}

	private void notPost() {
		System.out.println("3回試しましたが書きこみ出来ませんでした。");
		if(prop.getBool("KEEP_AUTOSTOP")) {
			System.out.println("保守を終了します。");
		}
	}
}
