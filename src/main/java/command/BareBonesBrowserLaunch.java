/*
 * Copyright Mindfire Solutions
 */
package command;

import java.util.Arrays;

import javax.swing.JOptionPane;

/**
 * The class to launch the browser with a given url.
 * 
 * @author Ashish Roy
 * @version 1.0
 * @since 27/07/2016
 *
 */
public class BareBonesBrowserLaunch {

	static final String[] BROWSERS = { "x-www-browser", "google-chrome", "firefox", "opera", "epiphany", "konqueror",
			"conkeror", "midori", "kazehakase", "mozilla" };
	static final String ERROR_MESSAGE = "Error attempting to launch web browser";

	/**
	 * Open the url in a browser
	 * 
	 * @param url
	 *            target Url
	 */
	public static void openURL(String url) {
		try { // attempt to use Desktop library from JDK 1.6+
			Class<?> d = Class.forName("java.awt.Desktop");
			d.getDeclaredMethod("browse", new Class[] { java.net.URI.class })
					.invoke(d.getDeclaredMethod("getDesktop").invoke(null), new Object[] { java.net.URI.create(url) });
			// above code mimicks: java.awt.Desktop.getDesktop().browse()
		} catch (Exception ignore) { // library not available or failed
			String osName = System.getProperty("os.name");
			try {
				if (osName.startsWith("Mac OS")) {
					Class.forName("com.apple.eio.FileManager")
							.getDeclaredMethod("openURL", new Class[] { String.class })
							.invoke(null, new Object[] { url });
				} else if (osName.startsWith("Windows"))
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				else { // assume Unix or Linux
					String browser = null;
					for (String b : BROWSERS)
						if (browser == null
								&& Runtime.getRuntime().exec(new String[] { "which", b }).getInputStream().read() != -1)
							Runtime.getRuntime().exec(new String[] { browser = b, url });
					if (browser == null)
						throw new Exception(Arrays.toString(BROWSERS));
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, ERROR_MESSAGE + "\n" + e.toString());
			}
		}
	}

}
