package copyx.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import copyx.pojo.ConfBean;

public class CopyXmain {
	
	private static ConfBean conf;
	static String confFile_ini = "conf.ini";
	static String confFile_json = "conf.json";
	static String filelistdir = "filelist\\";
	static Integer totalFile = 0;

	public static void main(String[] args) throws Exception {
		
		//读取配置---2个配置文件，json优先于ini
		File ini = new File(confFile_ini);
		File json = new File(confFile_json);
		if(json.exists()) {
			System.out.println("使用配置文件："+confFile_json+"\n");
			getconf4json();
		}else if(ini.exists()) {
			System.out.println("使用配置文件："+confFile_ini+"\n");
			getconfig4ini();
		}else {
			System.out.println("未找到配置文件！");
			return;
		}
		//检查code的配置
		if(!conf.getWebroot().containsKey(args[0])) {
			System.out.println("没有发现code相同的目标位置！");
			return;
		}
		System.out.println("替换目标webroot路径为："+conf.getWebroot().get(args[0])+"\n");
		//移动文件
		if(conf.getFilelist().size()>0) {
			String path = conf.getWebroot().get(args[0]);
			if(!path.substring(path.length()-1, path.length()).equals("\\"))	path += "\\";
			copyfile(path);
		}
		System.out.println("文件替换结束，共替换["+totalFile+"]个文件！");
	}

	private static void getconf4json() {
		FileReader f = null;
		try {
			f = new FileReader(confFile_json);
			BufferedReader bf = new BufferedReader(f);
			StringBuilder allstr = new StringBuilder();
			String str;
			while ((str = bf.readLine()) != null) {
				allstr.append(str);
			}
			bf.close();
			f.close();
			String all = allstr.toString();
			conf = JSON.parseObject(all, new TypeReference<ConfBean>() {});
			List<String> list = new ArrayList<String>();
			Set<String> keys = conf.getAll().keySet();
			for (String key : keys) {
				list.add(key);
			}
			conf.setFilelist(list);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private static void copyfile(String webroot) {
		String path = null;
		String fnamestr = null;
		try {
			for (String filename : conf.getFilelist()) {
				fnamestr = filename;
				String str = conf.getAll().get(filename);
				if(str!=null&&!"".equals(str)) {
					path = str;
				}else if(str!=null&&"\\".equals(str)) {
					path = "";
				}
				copydo(filelistdir,webroot+path+File.separator,filename);
			}
			System.out.println("\n");
		} catch (NoSuchFileException e) {
			System.out.println("在filelist中未找到:"+fnamestr+"\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private static void copydo(String source, String target, String filename) throws IOException {
		File file = new File(source+filename);
		if(file.isDirectory()) {//如果是文件夹，则特殊处理
			String[] filepath = file.list();
			for (String path : filepath) {
				copydo(source+File.separator+filename+File.separator,target+File.separator+filename+File.separator,path);
			}
		}else {
			File tdir = new File(target);
			if(!tdir.exists()) tdir.mkdirs();
			File tfile = new File(target+filename);
			if(tfile.exists())	tfile.delete();
			System.out.println(filename+"-->"+target+filename);
			totalFile++;
			Files.copy(file.toPath(), tfile.toPath());
		}
	}

	private static void getconfig4ini() {
		conf = new ConfBean();
		try {
			FileReader f = new FileReader(confFile_ini);
			BufferedReader bf = new BufferedReader(f);
			String str;
			Map<String,String> dir = new HashMap<String,String>();
			while ((str = bf.readLine()) != null) {
				if(str==null||"".equals(str)||str.indexOf("=")<0)	continue;
				String[] split = str.split("=");
				String key = split[0].toLowerCase();
				if(key.indexOf("webroot_")!=-1) {
					String codestr = key.substring(8, key.length());
					dir.put(codestr, split[1]);
				}
				if("filelist".equals(key)) {
					getpath(split[1]);
				}
			}
			conf.setWebroot(dir);
			bf.close();
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private static void getpath(String str) {
		if(str==null||"".equals(str))	return;
		LinkedHashMap<String, String> all = new LinkedHashMap<String,String>();
		List<String> filelist = new ArrayList<String>();
		for (String fileandpath : str.split(";")) {
			if(fileandpath==null||"".equals(fileandpath))	continue;
			String[] split = fileandpath.split(":");
			String file = split[0];
			if(split.length==2)	 all.put(file, split[1]);
			if(split.length==1)	all.put(file, "");
			filelist.add(file);
		}
		conf.setAll(all);
		conf.setFilelist(filelist);
	}

}
