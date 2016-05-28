package dosn.search.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class is to control the opened connections to the potential servers.
 * 
 */
public class PotentielServerConnectionResultListener {

	private List<Long> finishedThreads = null;
	private List<String> jsonObjects = null;
	private Integer limitToNotify = null;

	public PotentielServerConnectionResultListener() {
		jsonObjects = new ArrayList<String>();
		finishedThreads = new ArrayList<Long>();
	}

	public PotentielServerConnectionResultListener(Integer limitToNotify) {
		jsonObjects = new ArrayList<String>();
		finishedThreads = new ArrayList<Long>();
		this.limitToNotify = limitToNotify;
	}

	public List<String> getJsonObjects() {
		return jsonObjects;
	}

	public void addJsonObject(String jsonObject) {
		// System.out.println(this.getClass().getName()+": json object added");
		this.jsonObjects.add(jsonObject);
	}

	public List<Long> getFinishedThreads() {
		return finishedThreads;
	}

	/**
	 * This method notifies the controller upon receiving the response from the
	 * potential servers with opened connections
	 * 
	 * @param finishedThread
	 */
	public void addFinishedThread(Long finishedThread) {
		// System.out.println(this.getClass().getName()+": a thread finished");
		this.finishedThreads.add(finishedThread);
		if (this.limitToNotify != null
				&& this.finishedThreads.size() == this.limitToNotify) {
			// System.out.println(this.getClass().getName() + ": to notify");
			synchronized (this) {
				this.notify();
			}
			// System.out.println(this.getClass().getName() +
			// ": notification DONE");
			limitToNotify = null;
		}
	}

}
