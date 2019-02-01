package pkg.util;

import java.io.File;
import java.io.IOException;

import org.snowink.bouyomichan.BouyomiChan4J;

public class Voice {
	private int port;
	private Process bcproc;

	private BouyomiChan4J bc;

	public Voice() {
		this(50001);
	}

	public Voice(int port) {
		this.port = port;
		bc = new BouyomiChan4J("127.0.0.1", this.port);
	}

	public void run( String exepath ) throws IOException {
		File file = new File(exepath);
		String[] Command = { file.getPath() };

		Runtime runtime = Runtime.getRuntime();
		bcproc = runtime.exec(Command);
	}

	public void destroy() {
		bcproc.destroy();
	}

	public void addText(String str) {
		bc.talk(str);
	}
}
