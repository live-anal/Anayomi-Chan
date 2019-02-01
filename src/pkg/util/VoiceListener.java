package pkg.util;

import main.com.j5.connect.Content;

public interface VoiceListener {

	boolean addText(String msg);

	boolean addText(Content c, int num);

	void down();

	boolean addText(String string, String message);

}
