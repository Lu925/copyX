package copyx.pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfBean {
	
	private Map<String,String> webroot;
	private LinkedHashMap<String,String> all;
	private List<String> filelist;
	public Map<String, String> getWebroot() {
		return webroot;
	}
	public void setWebroot(Map<String, String> webroot) {
		this.webroot = webroot;
	}
	public List<String> getFilelist() {
		return filelist;
	}
	public void setFilelist(List<String> filelist) {
		this.filelist = filelist;
	}
	public LinkedHashMap<String, String> getAll() {
		return all;
	}
	public void setAll(LinkedHashMap<String, String> all) {
		this.all = all;
	}

	
	

}
