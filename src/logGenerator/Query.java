package logGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Jayanthy
 * This application accomplishes the following tasks by establishing a 
 * JDBC connection to the MySQL database and thereby querying the table
 * loadData method loads the log data from the csv files to the database table
 * searchData method lets you query the table and return the average CPU usage
 *
 */
public class Query {

	/**
	 * This method establishes a connection to the MySQL database
	 * using JDBC and returns an instance of JDBC connection object
	 * @param Nothing
	 * @return JDBC connection
	 */

	private static Connection getDatabaseConnection() {
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
		String url = prop.getProperty("url");
		String dbName = prop.getProperty("dbName");
		String driver = prop.getProperty("driver");
		String userName = prop.getProperty("userName");
		String password = prop.getProperty("password");
		
		Connection conn = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	/**
	 * This method loads the data from the given directory into the MySQL table
	 * Each csv file is loaded in a batch until all the 1000 files for the 
	 * 1000 servers are loaded.It then calls createIndex method
	 * The average runtime of this method is 2.5-3.5 minutes approximately
	 * @param directory
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void loadData(String directory) throws SQLException, IOException {	
		
		Connection conn = getDatabaseConnection();	
		//Creating table		
		String dropTableQuery = "DROP TABLE IF EXISTS `log_info_csv`";
		String createTableQuery = "CREATE TABLE `log-manager`.`log_info_csv` (`timestamp` long NOT NULL," +
				" `server_id` varchar(15) NOT NULL,`cpu_id` int(1) NOT NULL," + 
				"  `cpu_load` int(2) NOT NULL)";
		Statement stm = conn.createStatement();
		stm.execute(dropTableQuery);
		stm.execute(createTableQuery);
		
		// Loading data into the table
		File path = new File(directory);
		for (File fileEntry : path.listFiles()) {
			String fileName = directory+"/"+fileEntry.getName();
			try {
				String setInFile = "SET GLOBAL local_infile = true";
				String loadQuery = "LOAD DATA LOCAL INFILE '"+fileName+"' INTO TABLE `log-manager`.`log_info_csv` "
						+ "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
				Statement stmt = conn.createStatement();
				stmt.execute(setInFile);
				stmt.execute(loadQuery);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		createIndex();
		conn.close();
	}

	/**
	 * This method indexes the log_info_csv table which has almost 
	 * 2.88M rows of data. This ensures that searches on this huge table
	 * is less time consuming. This method is called by loadData method.
	 * The average runtime of this method is 2-3 minutes approximately
	 */

	public static void createIndex() {
		Connection conn = getDatabaseConnection();
		String indexQuery = "CREATE INDEX log_info_index ON `log-manager`.`log_info_csv`(server_id)";		
		try {
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate(indexQuery);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * This method returns the average CPU usage for the given server ID
	 * within the given start and end times. A call is made to the 
	 * getDatabaseConnection method to get a JDBC connection. The output
	 * of the search query is printed on the console  
	 * @param server_id The server ID for which we need CPU usage
	 * @param startDate The start time
	 * @param endDate The end time
	 */
	private static void searchData(String server_id, String cpu_id, long startDate, long endDate) {
		TimeStampFormatter strToUnixConvObj = new TimeStampFormatter();
		Connection conn = getDatabaseConnection();
		String searchQuery = "SELECT  timestamp, avg(cpu_load) as sum FROM `log-manager`.log_info_csv" +
				" WHERE server_id='"+server_id+"'" +  
				" AND cpu_id="+cpu_id+
				" AND timestamp between "+ startDate +" and "+ endDate +
				" GROUP BY timestamp";
		try {
			Statement stmt = conn.createStatement();
			//long s = System.currentTimeMillis();
			ResultSet rs = stmt.executeQuery(searchQuery);
			System.out.println("CPU" + cpu_id + " Usage on " + server_id + ":");
			while(rs.next())
			{
				long ts = rs.getLong("timestamp");
				String date = strToUnixConvObj.convertUnixtoString(ts);
				System.out.print("(" +date +","+rs.getInt("sum")+"%)");
				if(!rs.isLast())
					System.out.print(",");
			}
			conn.close();
			System.out.println();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the main method that accepts a command line argument 
	 * which is the directory path. The first time the program is run, we 
	 * have to make sure the data is loaded, else the application will exit
	 * If the argument is a directory path, loadData method will be called
	 * and the system will be ready to accept queries. The format for quering
	 * is QUERY <cpu-id> <start-time(yyyy-mm-dd hh:mm)> <end-time(yyyy-mm-dd hh:mm)>
	 * In order to exit out of the system, you can type "exit" on the command
	 * prompt for the application to exit 
	 * @param args directory path
	 * @throws SQLException 
	 * @throws IOException
	 */

	public static void main(String[] args) throws SQLException, IOException {
		if(args.length < 1) {
			System.out.println("Usage: query.jar DATA_PATH");
			System.exit(0);
		}

		String directory = null;
		if(args.length == 1) {
			directory = args[0];
			LogGeneratorProperties logGenObj = new LogGeneratorProperties();
			loadData(directory);			
		}

		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.println(">");
			String input = scan.nextLine();
			String[] arrInput = input.split(" ");

			// Do query
			if(arrInput[0].equalsIgnoreCase("QUERY")) {
				String server_id = arrInput[1];
				String cpu_id = arrInput[2];
				String start = arrInput[3] + " "+ arrInput[4];
				String end = arrInput[5]+" " +arrInput[6];
				TimeStampFormatter strToUnixConvObj = new TimeStampFormatter();

				long startDate = strToUnixConvObj.convertStringtoUnix(start);
				long endDate = strToUnixConvObj.convertStringtoUnix(end)-1l;

				long s = System.currentTimeMillis();
				searchData(server_id,cpu_id,startDate,endDate);
				System.out.println("\nTIME TO QUERY");
				System.out.println(System.currentTimeMillis()-s);
			} 

			else if(arrInput[0].equalsIgnoreCase("EXIT")) {
				break;
			}
		}
		scan.close();
	}
}