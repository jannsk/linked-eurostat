package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class FileLoader {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	private URL url;
	private String escapedUrl;
	private File directory;
	
	public FileLoader(URL url) {
		this.url = url;
		this.escapedUrl = url.toString().replace("//", "").replace("/", "");
	}
	
	public InputStream get() throws IOException {
		// Change to true if you are using gae
		if (false) {
			return loadUrl();
		} else {
			if (!isInCache()) {
				saveToCache(loadUrl());
			} else {
				_log.info("Served from cache");
			}
			return getFromCache();
		}
	}
	
	private InputStream loadUrl() throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		
		// Workaround since user agent java is blocked by Eurostat.
		conn.setRequestProperty("User-agent", "notjava");
		conn.setConnectTimeout(900*1000);
		conn.setReadTimeout(900*1000);
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("lookup on " + url + " resulted HTTP in status code " + conn.getResponseCode());
		}
		return conn.getInputStream();
	}
	
	private void saveToCache(InputStream inputStream) throws IOException {
		File file = new File(directory.getAbsolutePath()+"/"+this.escapedUrl);
		file.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(file);
		
		_log.info("Caching to "+file.getAbsolutePath());
		
		try {
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} finally {
			outputStream.close();
		}
	}
	
	private InputStream getFromCache() throws FileNotFoundException {
		File file = new File(directory.getAbsolutePath()+"/"+this.escapedUrl);
		return new FileInputStream(file);
	}
	
	private boolean isInCache() {
		directory = new File("estatwrap-cache");
		directory.mkdirs();
		File file = new File(directory.getAbsolutePath()+"/"+this.escapedUrl);
		if (file.exists() && (file.lastModified() > (System.currentTimeMillis() - (24*60*60)))) {
			return true;
		} else {
			file.delete();
			return false;
		}
	}
	
}
