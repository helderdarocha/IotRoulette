package iot.web.roulette;

import java.io.IOException;
import java.io.InputStream;

public interface DataLoader {
	void load(InputStream stream) throws IOException;
	void addItem(String item);
	String[] getData();
}
