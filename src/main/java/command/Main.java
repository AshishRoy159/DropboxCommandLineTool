/*
 * Copyrigt Mindfire Solutions 2016
 */

package command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

/**
 * This Class is used to upload files and folder to dropbox using command line
 * 
 * @author Ashish Roy
 * @version 1.0
 * @since 26/07/2016
 */
public class Main {
	
	private static final String ACCESS_TOKEN = "qgRCszXpPxgAAAAAAAAAOhwDF7RiUTC42S5HSy6wume-yRfTshmjmQUJXeretYzY";
	private static final String FILE = "file";
	private static final String FOLDER = "folder";

	/**
	 * This is the main method.
	 * 
	 * @param args
	 *            for command line arguments
	 * @throws DbxException
	 *             Dropbox Exception
	 * @throws IOException
	 *             IOException
	 */
	public static void main(String args[]) throws DbxException, IOException {

		// check if command line arguments are present
		if (args.length != 2) {
			System.out.println("Usage: COMMAND <type> <file-path>");
			System.out.println("");
			System.out.println("<type>: type of input ( 'file' / 'folder' ).");
			System.out.println("");
			System.out.println("<file path>: Fully qualified local file path.");
			System.out.println("");
			System.out.println("Example for linux: java -jar command.jar file /home/mindfire/Desktop/auth.txt");
			System.out.println("");
			System.exit(1);
			return;
		}

		// Create Dropbox client
		DbxRequestConfig config = new DbxRequestConfig("CommanTool/v1.0");
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

		// Get current account info
		FullAccount account = client.users().getCurrentAccount();
		System.out.println(account.getName().getDisplayName());

		// Assing command line inputs to String
		String type = args[0];
		String newFile = args[1];

		// Check the contents
		System.out.println(type);
		System.out.println(newFile);

		// Check for file or folder
		if (type.equals(FILE)) {
			String folder = "";
			// upload file
			uploadFile(client, newFile, folder);
		} else if (type.equals(FOLDER)) {
			String foldername = "";
			// upload folder
			uploadFolder(client, newFile, foldername, false);
		}

		// Get files and folder metadata from Dropbox root directory
		ListFolderResult result = client.files().listFolder("");
		while (true) {
			for (Metadata metadata : result.getEntries()) {
				System.out.println(metadata.getPathLower());
			}
			if (!result.getHasMore()) {
				break;
			}
			result = client.files().listFolderContinue(result.getCursor());
		}
	}

	/**
	 * This method is used to upload file in dropbox storage
	 * 
	 * @param client
	 *            current user
	 * @param newFile
	 *            input file
	 * @param folder
	 *            folder name
	 * @throws DbxException
	 *             Dropbox Exception
	 * @throws IOException
	 *             IO Exception
	 */
	public static void uploadFile(DbxClientV2 client, String newFile, String folder) throws DbxException, IOException {
		int position = newFile.lastIndexOf('/');
		String filename = newFile.substring(position);
		System.out.println(filename);

		if (!(folder.equals("") || folder.equals(null))) {
			filename = folder + filename;
			System.out.println(filename);
		}

		Metadata existingFile = null;
		try {
			existingFile = client.files().getMetadata(filename);
		} catch (Exception e) {

		}
		if (existingFile != null) {
			String rev = ((FileMetadata) existingFile).getRev();
			try (InputStream in = new FileInputStream(newFile)) {
				FileMetadata metadata = client.files().uploadBuilder(filename).withMode(WriteMode.update(rev))
						.uploadAndFinish(in);

				System.out.println(filename);
				System.out.println("------------------------------------------");
				System.out.println(metadata.getName());
				System.out.println(metadata.getMediaInfo());
				System.out.println(metadata.getPathDisplay());
				System.out.println("------------------------------------------");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Upload Error!!");
			}
		} else {
			try (InputStream in = new FileInputStream(newFile)) {
				FileMetadata metadata = client.files().uploadBuilder(filename).uploadAndFinish(in);

				System.out.println(filename);
				System.out.println("------------------------------------------");
				System.out.println(metadata.getName());
				System.out.println(metadata.getMediaInfo());
				System.out.println(metadata.getPathDisplay());
				System.out.println("------------------------------------------");
			} catch (Exception e) {
				System.out.println("Upload Error!!");
			}
		}

	}

	/**
	 * Upload folder and it's contents to dropbox
	 * 
	 * @param client
	 *            current user
	 * @param newFile
	 *            folder to upload
	 * @param foldername
	 *            folder name where the file is to be uploaded
	 * @param nestedFolder
	 *            is nestedfolder
	 * @throws DbxException
	 *             Dropbox Exception
	 * @throws IOException
	 *             IO Exception
	 */
	public static void uploadFolder(DbxClientV2 client, String newFile, String foldername, boolean nestedFolder)
			throws DbxException, IOException {
		// Check if nested folder or not
		if (!nestedFolder) {
			int position = newFile.lastIndexOf('/');
			foldername = newFile.substring(position);
			System.out.println(foldername);
		} else {
			foldername = foldername + newFile.substring(newFile.lastIndexOf("/"));
			System.out.println("Newfile:" + newFile);
			System.out.println(foldername);
		}

		// check if the folder exists or not
		Metadata folder2 = null;
		try {
			folder2 = client.files().getMetadata(foldername);
		} catch (Exception e) {

		}

		// If folder does not exists create folder
		if (folder2 == null) {
			System.out.println("Folder doesn't exists!! Creating Folder!");
			FolderMetadata metadata = client.files().createFolder(foldername);
			System.out.println(metadata.getName());
		}

		// get contents of the folder
		File folder = new File(newFile);
		System.out.println(folder);
		File[] listOfFiles = folder.listFiles();

		// upload sub-folder or files
		for (File file : listOfFiles) {
			if (file.isFile()) {
				System.out.println("File " + file.getAbsolutePath());
				uploadFile(client, file.getAbsolutePath(), foldername);
			} else if (file.isDirectory()) {
				System.out.println("Directory " + file.getAbsolutePath());
				uploadFolder(client, file.getAbsolutePath(), foldername, true);
			}
		}
	}
}