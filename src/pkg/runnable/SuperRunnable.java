package pkg.runnable;

import java.util.List;

import main.com.j5.connect.Content;
import main.com.j5.connect.J5ch;
import pkg.util.LinkedProperties;
import pkg.util.VoiceListener;

public abstract class SuperRunnable implements Runnable{
	protected J5ch j5;
	protected LinkedProperties prop;
	protected List<String> nglist;
	protected List<String> ngthread;
	protected String key;
	protected VoiceListener lis;

	protected String[] ng_stack;

	public SuperRunnable(VoiceListener lis, int ng_num) {
		this.lis = lis;
		ng_stack = new String[ng_num];
	}

	public void setNGStack(int num) {
		ng_stack = new String[num];
	}

	public void setJ5ch(J5ch j5) {
		this.j5 = j5;
	}

	public void setProperties(LinkedProperties prop) {
		this.prop = prop;
	}

	public void setNGList(List<String> nglist) {
		this.nglist = nglist;
	}

	public void setNGThread(List<String> ngthread) {
		this.ngthread = ngthread;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean addText(String msg) {
		return lis.addText(msg);
	}

	public boolean addText(Content c, int num) {
		ng_stack[num%ng_stack.length] = c.getMessage();

		// ngnumレス分比較
		boolean f = true;
		String first = ng_stack[num%ng_stack.length];

		for (int i = 0; i< ng_stack.length && f; i++) {
	        if(ng_stack[i]==null || !ng_stack[i].equals(first)) {
	        	f = false;
	        }
	    }

		// trueであればNGリストに追加
		if(f && !nglist.contains(first)) {
			nglist.add(first);
		}

		return lis.addText(c, num) || f;
	}

	public void down() {
		lis.down();
	}

	public boolean checkNG(String msg) {
		boolean f = false;

		for(String ng : nglist) {
			if(msg.contains(ng) || msg.matches(ng)) {
				f = true;
				break;
			}
		}

		return f;
	}

	public void addText(String string, String message) {
		lis.addText(string, message);
	}
}
