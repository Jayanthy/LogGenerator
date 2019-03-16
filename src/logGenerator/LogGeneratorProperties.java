package logGenerator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/** 
 * @author Jayanthy
 * This class includes all the static property value used within the application.
 * Executing this class creates log.properties files in the project root directory
 */
public class LogGeneratorProperties {

	LogGeneratorProperties() {
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream("log.properties");
			// set the properties value
			prop.setProperty("url", "jdbc:mysql://127.0.0.1:3306/");			
			prop.setProperty("dbName", "log-manager?verifyServerCertificate=false&useSSL=true");
			prop.setProperty("driver", "com.mysql.jdbc.Driver");
			prop.setProperty("userName", "patientportal");
			prop.setProperty("password", "portal");
			prop.setProperty("start", "2017-11-20 06:35");
			prop.setProperty("end", "2017-11-21 06:35");

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}