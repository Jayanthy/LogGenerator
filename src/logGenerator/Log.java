package logGenerator;
import java.io.Serializable;

/**
 * @author Jayanthy
 * Specifies the data contained in each record of the log stored in a file or a database table
 * Includes a constructor with all fields to initialize
 * Includes a toString method used by Generate.java while writing the logs to a file 
 */

public class Log implements Serializable{
	
	private static final long serialVersionUID = 1L;
	long logdate;
	String serverId;
	int cpuId;
	int usage;
	
	
	/**
	 * This is the constructor with all the 4 attributes
	 * @param time  Epoch time stamp of datatype long
	 * @param serverId  The server's ID value represented like xxx.xxx.xxx.xxx
	 * @param cpuId  The CPU's ID. It can be 0 or 1
	 * @param usage  Denotes of the CPU load
	 * @return Nothing
	 */
	
	public Log(long time, String serverId, int cpuId, int usage) {
		this.logdate = time;
		this.serverId = serverId;
		this.cpuId = cpuId;
		this.usage = usage;
	}
	
	/**
	 * This method returns the String representation of the class attributes
	 */
	
	@Override
	public String toString() {
		return logdate +","+serverId+","+ cpuId + ","+ usage;
	}
}
