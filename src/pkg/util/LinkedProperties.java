package pkg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

public class LinkedProperties {
	private File file;
	List<Pair<String, String>> values;

	public LinkedProperties(String path) {
		this(new File(path));
	}

	public LinkedProperties(File file) {
		this.file = file;
		values = new LinkedList<>();

		open();
	}

	private void open() {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

			for(String line=br.readLine(); line!=null; line=br.readLine()) {
				read(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(String line) {
		String[] pair = line.split("=",2);

		if(!set(pair[0], pair[1])) {
			values.add(new Pair<>(pair[0], pair[1]));
		}
	}

	private String get(String key) {
		String value = "";

		for(Pair<String,String> pair : values) {
			if(pair.getKey().equals(key)) {
				value = pair.getValue();
				break;
			}
		}

		return value;
	}

	public int getInt(String key) throws NumberFormatException{
		String value = get(key);

		return ((value.isEmpty()) ? 0 : Integer.parseInt(value));
	}

	public boolean getBool(String key) throws NumberFormatException{
		String value = get(key);

		return ((value.isEmpty() || !value.equals("1")) ? false : true);
	}

	public String getString(String key) {
		return get(key);
	}

	public boolean set(String key, String value) {

		boolean f = false;
		for(int i=0; i<values.size(); i++) {
			Pair<String, String> v = values.get(i);

			if(v.getKey().equals(key)) {
				values.set(i, new Pair<>(key, value));
				f = true;
				break;
			}
		}

		return f;
	}

	public void write() {
		String path = file.getPath();

		file.delete();
		file = new File(path);

		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))){
			for(Pair<String,String> pair : values) {
				bw.write(pair.getKey()+"="+pair.getValue()+"\r\n");
				bw.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String buf = "";

		for(Pair<String,String> pair : values) {
			buf += pair.getKey()+":\t"+pair.getValue()+"\n";
		}

		return buf;
	}
}
