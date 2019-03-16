package logGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

/**
 * @author Jayanthy
 * This program implements an application that generates server logs for 1000 servers
 * There is a start and end UNIX time stamp.  for which the log is generated
 * Each server has 2 CPUs and the log includes information about CPU usage for both CPUs every minute.
 * Each record of the log has information about the Server ID, epoch time stamp, CPU ID and CPU usage
 * The application includes a command line argument which is directory path
 * The class implements Runnable interface in order to use Java Threads. Hence it overrides the 
 * required run method which is invoked when start method is called on a thread
 */

public class Generate implements Runnable{
	private Thread t;
	private String threadName;
	FileWriter fw;
	File newTextFile;
	long startDate;
	long endDate;
	Random rand = new Random();


	/**
	 * This method initializes various parameters required for generating the log file for each server
	 * This function is called once by each server making it 1000 total calls
	 * The name of the log file for each server is defaulted to the server name
	 * @param name Server name which is in the format 192.168.xxx.xxx
	 * @param path Command line argument which is the directory path
	 * @param startDate The start date for generating logs represented in UNIX EPOCH time
	 * @param endDate The end date for generating logs represented in UNIX EPOCH time
	 */

	Generate(String name, String path, long startDate, long endDate)  {
		this.threadName = name;
		this.startDate = startDate;
		this.endDate = endDate;

		// File name formatting - directory path + name of the server
		newTextFile = new File(path+"\\"+threadName+".csv");
		try {
			fw = new FileWriter(newTextFile,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is invoked when the start method is called on a thread
	 * This method uses the Log calls in order to set the log details for 
	 * each record by calling the Log class constructor. The log information is 
	 * then written to the file using File Writer after calling toString method of Log class
	 * This is repeated until the startDate become equal to the endDate
	 * in which case the execution for this thread will end and the 
	 * file writer instance will be closed.
	 * @exception IOException while writing to file
	 */
	
	@Override
	public void run() {	
		while(startDate < endDate) {
			Log l1 = new Log(startDate,threadName,0,rand.nextInt(100));
			Log l2 = new Log(startDate,threadName,1,rand.nextInt(100));

			try {
				fw.write(l1.toString());
				fw.write(System.getProperty("line.separator"));
				fw.write(l2.toString());
				fw.write(System.getProperty("line.separator"));
			} catch (IOException iox) {
				iox.printStackTrace();
			}
			startDate = startDate+60;
		}
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method creates a new thread object every time it is called 
	 * in case the thread does'nt already exist
	 * It then calls the start method on the thread which in turn invokes the run() method
	 * @param nothing
	 * @return nothing
	 */
	public void start() {
		if(t == null) {
			t = new Thread(this,threadName);
			t.start();
		}
	}

	/**
	 * This is the main method which uses the Generate, start and run methods to generate server logs
	 * Also makes use of the TimeStampFormatter class to format start and end date for the logs 
	 * Creates 1000 threads which will run in parallel to generate the log files for 1000 servers
	 * @param args  Directory path, if null, the application will halt
	 * @return Nothing
	 */
	public static void main(String[] args) {

		// 
		if(args.length < 1) {
			System.out.println("Usage: Generate.jar DATA_PATH");
			System.exit(0);
		}

		String path = args[0];
		TimeStampFormatter strToUnixConvObj = new TimeStampFormatter();
		LogGeneratorProperties logGenObj = new LogGeneratorProperties();
		Properties prop = new Properties();
		InputStream input;
		
		try {
			input = new FileInputStream("log.properties");
			prop.load(input);
		} catch (FileNotFoundException e1) {		
			e1.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		String start = prop.getProperty("start");
		String end = prop.getProperty("end");

		long startDate = strToUnixConvObj.convertStringtoUnix(start);
		long endDate = strToUnixConvObj.convertStringtoUnix(end);
		int j = 1;
		int k = 0;

		/** 
		 * Generating server ID of the form 192.168.xxx.xxx (max - 255.255.255.255)
		 * The 3rd segment xxx is initially kept as a constant value 1 
		 * The 4th segment xxx can go from 1 to 255. Once it reaches 255, 
		 * the 3rd segment is incremented until it reached 255.
		 * For 1000 servers, the last IP is 192.168.4.235
		 * The loops creates 1000 threads which will run in parallel to
		 * generate the log files for 1000 servers. The parameters for threadObj
		 * is set by calling the Generate method with the required arguments
		 * Once the thread is ready, we call the start method to execute
		 */
		for (int i = 0; i < 1000; i++) {
			if(k < 255)
				k++;
			else { 
				j++;
				k=1;
			}
			Generate threadObj = new Generate("192.168."+j+"."+(k),path,startDate,endDate);
			threadObj.start();
		}
	}
}
