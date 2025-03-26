package org.gh;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Application {
	private static final String url = "https://keyauth.win/api/1.1/";

	// Replace with your own key auth
	private static String ownerid = ""; // You can find out the owner id in the profile settings keyauth.com
	private static String appname = ""; // Application name
	private static String version = ""; // Application version
	private final KeyAuth keyAuth;

	public Application() {
		keyAuth = new KeyAuth(appname, ownerid, version, url);
	}

	public void validateKey(String key, Runnable onSuccess) {
		// Call the init() method first
		keyAuth.init();

		boolean isValid = keyAuth.license(key);

		if (isValid) {
			onSuccess.run();
		} else {
			JOptionPane.showMessageDialog(null, "Invalid license key", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException {


		// Used to delete temp chrome profiles made to launch browser
		String userName = System.getProperty("user.name");
		File dir = new File("C:\\Users\\" + userName + "\\AppData\\Local\\Temp");
		deleteDirectory(dir);

		Application app = new Application();
		LoginUi loginUi = new LoginUi();
		loginUi.setupUi(app);

		// Display the login UI
		loginUi.setVisible(true);
	}

	private static void deleteDirectory(File dir) {
		File[] allContents = dir.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		try {
			dir.delete();
		} catch (SecurityException e) {
			System.out.println("Unable to delete " + dir + ". Skipping.");
		}
	}

}
