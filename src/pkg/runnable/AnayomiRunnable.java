package pkg.runnable;

import java.util.List;

import main.com.j5.connect.Content;
import main.com.j5.connect.J5ch;
import main.com.j5.connect.ResultSet;
import main.com.j5.exception.UnkoException;
import pkg.util.LinkedProperties;
import pkg.util.VoiceListener;

public class AnayomiRunnable extends SuperRunnable{
	public AnayomiRunnable(J5ch j5, LinkedProperties prop, List<String> nglist, int ng_num, VoiceListener v) {
		super(v, ng_num);
		setJ5ch(j5);
		setProperties(prop);
		setNGList(nglist);
	}

	@Override
	public void run() {
		boolean run = true;
		ResultSet before;
		int num = 1;
		int pausetime = prop.getInt("PAUSE_TIME");
		boolean yomiage_all = prop.getBool("YOMIAGE_ALL");

		try {
			before = j5.get(key);

			for(Content c : before.getContent()) {
				if(yomiage_all) {
					addText(c, num);
				}
				num++;
			}
		}catch(UnkoException e) {
			before = new ResultSet();
		}

		int recnt = 0;
		while(run) {
			try {
				ResultSet after = j5.get(key, before.lastmodify, before.bytes);

				// 更新
				if(after.size() > 0) {

					// スレ落ち
					if(after.size() > 3 && after.get(2).contains("ＥＲＲＯＲ")) {

						run = false;
						down();

					} else {
						// レス更新
						for(Content c : after.getContent()) {
							if( !addText(c, num) ) {
								Thread.sleep(pausetime);
							}
							num++;
						}

						before.lastmodify = after.lastmodify;
						before.bytes += after.bytes;
						recnt = 0;
					}
				}else {
					Thread.sleep(pausetime);
				}

			}catch(UnkoException | NumberFormatException e) {
				recnt++;

				if(recnt > 3) {
					run = false;
					down();
				}
			}catch(InterruptedException e) {
				run = false;
			}
		}
	}

	public boolean addText(Content c, int num) {
		if(!checkNG(c.getMessage()))
			return super.addText(c, num);
		else
			return false;
	}
}
