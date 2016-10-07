package com.android.rockchip.cifs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.rockchip.FileInfo;
import com.android.rockchip.R;
import com.android.rockchip.RockExplorer;
import com.android.rockchip.EnumConstent;


import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.EthernetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;



public class SmbUtil {
	private static final String TAG="SmbUtil";
	private static final boolean DEBUG = true;//true;
	
	private static void LOG(String str)
	{
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}
	
	private static final int MSG_EXPAND_GROUP = 0;
	private static final String KEY_GROUP_ID = "group";
	public static final String smb_dir = "S M B";
	public static final String SMB_MOUNTPOINT_ROOT = "/data/smb";
	public static final String SHELL_ROOT = "/data/etc";
	
	public static final String SHELL_PATH = "/data/etc/cifsmanager.sh";
	public static final String SHELL_LOG_PATH = "/data/etc/log";
	public static final String SHELL_HEAD = "#!/system/bin/sh";
	
	private static Resources res;
	
	private RockExplorer mRockExplorer;
	private Handler mHandler;
		
	private boolean isSearchOver;

	private int MAX_THREAD = 16;
	
	private int threadStart = 0;
	private int threadOver = 0;
	
	private int AllHostCount = 0;
	private int ScannedHostCount = 0;
	
	
	public SmbUtil(RockExplorer mRockExplorer,Handler handler) {
		// TODO Auto-generated constructor stub
		mRockExplorer = mRockExplorer;
		mHandler = handler;
		res = mRockExplorer.getResources();

	}
	
	public static String calcMaskByPrefixLength(int length) {
		int mask = -1 << (32 - length);
		int partsNum = 4;
		int bitsOfPart = 8;
		int maskParts[] = new int[partsNum];
		int selector = 0x000000ff;

		for (int i = 0; i < maskParts.length; i++) {
			int pos = maskParts.length - 1 - i;
			maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
		}

		String result = "";
		result = result + maskParts[0];
		for (int i = 1; i < maskParts.length; i++) {
			result = result + "." + maskParts[i];
		}
		LOG("cal mask:"+result);
		return result;
	}

	
	public ArrayList<String> getIpAndMask() {
		ArrayList<String> maskAndipList = new ArrayList<String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				List<InterfaceAddress> listIAddr = intf.getInterfaceAddresses();
				Iterator<InterfaceAddress> IAddrIterator = listIAddr.iterator();
				while (IAddrIterator.hasNext()) {     
					InterfaceAddress IAddr = IAddrIterator.next();
					InetAddress inetAddress = IAddr.getAddress();
					if (!inetAddress.isLoopbackAddress()) {
						String ip = inetAddress.getHostAddress();
						LOG("ip:"+ip);
						String subnetmask = calcMaskByPrefixLength(IAddr.getNetworkPrefixLength());
						LOG("mask:"+subnetmask);
						String ipAndmask = ip+";"+subnetmask;
						maskAndipList.add(ipAndmask);
					}  
				}   
			}      
		} catch (SocketException ex) {
			Log.e("network card info", ex.toString());
			return maskAndipList;
		}
		return maskAndipList; 
	}
	

	
	public Vector<Vector<InetAddress>> getsubnetAddress(){
		int subnetmaskParts[] = new int[4];
		int hostParts[] = new int[4];
		int subnetStart[] = new int[4];
		int subnetEnd[] = new int[4];
		Vector<Vector<InetAddress>> vectorlist = new Vector<Vector<InetAddress>>();
		ArrayList<String> maskAndipList = getIpAndMask();
		
		
		for (int m =0;m<maskAndipList.size();m++){
			String maskAndipsplit[] = maskAndipList.get(m).split(";");
			String host = maskAndipsplit[0];
			String subnetmask = maskAndipsplit[1];
	try{
		String split[] = host.split("\\.");
		hostParts[0]=Integer.parseInt(split[0]);
		hostParts[1]=Integer.parseInt(split[1]);
		hostParts[2]=Integer.parseInt(split[2]);
		hostParts[3]=Integer.parseInt(split[3]);
		
		LOG("host part:"+hostParts[0]+" "+hostParts[1]+" "+hostParts[2]+" "+hostParts[3]);
		
		split = subnetmask.split("\\.");
		subnetmaskParts[0]=Integer.parseInt(split[0]);
		subnetmaskParts[1]=Integer.parseInt(split[1]);
		subnetmaskParts[2]=Integer.parseInt(split[2]);
		subnetmaskParts[3]=Integer.parseInt(split[3]);
		LOG("subnetmask part:"+subnetmaskParts[0]+" "+subnetmaskParts[1]+" "+subnetmaskParts[2]+" "+subnetmaskParts[3]);
		subnetStart[0] = subnetmaskParts[0] & hostParts[0];
		subnetStart[1] = subnetmaskParts[1] & hostParts[1];
		subnetStart[2] = subnetmaskParts[2] & hostParts[2];
		subnetStart[3] = subnetmaskParts[3] & hostParts[3];
		LOG("subnet start:"+subnetStart[0]+" "+subnetStart[1]+" "+subnetStart[2]+" "+subnetStart[3]);
		
		subnetEnd[0] = subnetStart[0] | (subnetmaskParts[0] ^ 0xFF);
		subnetEnd[1] = subnetStart[1] | (subnetmaskParts[1] ^ 0xFF);
		subnetEnd[2] = subnetStart[2] | (subnetmaskParts[2] ^ 0xFF);
		subnetEnd[3] = subnetStart[3] | (subnetmaskParts[3] ^ 0xFF);
		LOG("subnet end:"+subnetEnd[0]+" "+subnetEnd[1]+" "+subnetEnd[2]+" "+subnetEnd[3]);
	}catch(Exception ex){}
		Vector<InetAddress> vector = new Vector<InetAddress>();
		for (int i = subnetStart[0];i<= subnetEnd[0];i++)
			for (int j = subnetStart[1];j<= subnetEnd[1];j++)
				for(int k = subnetStart[2];k<=subnetEnd[2];k++)
					for(int l = subnetStart[3];l<=subnetEnd[3];l++){
						byte subnetaddress[] = new byte[4];
						subnetaddress[0] = (byte)i;
						subnetaddress[1] = (byte)j;
						subnetaddress[2] = (byte)k;
						subnetaddress[3] = (byte)l;
						try{
							InetAddress inetaddress = InetAddress.getByAddress(subnetaddress);
							vector.add(inetaddress);
							AllHostCount++;
						}catch (Exception e) {
							// TODO: handle exception
						}
					}
		vectorlist.add(vector);
		}
		return vectorlist;
	}
	
	private class ConnectHost implements Runnable{
		private InetAddress host;
		private int timesDroped = 0;
		/**
		 * @param host
		 */
		public ConnectHost(InetAddress host) {
			super();
			this.host = host;
		}
		
		public boolean ping(InetAddress inet){
			try  {				
				Socket server = new Socket();
				int port = 445;
				if (timesDroped == 1){
					port = 139;
				}
				LOG("connect ip:"+inet.getHostAddress()+ " port:"+port+" timedroped:"+timesDroped);
		        InetSocketAddress address = new InetSocketAddress(inet,port);
		        server.connect(address,500);
		        server.close();
		    } catch (UnknownHostException e){
			    if (timesDroped < 1){
		    		timesDroped++;
		    	}else{
		    		return false;
		    	}
			    
			    if (ping(inet)){
			    	return true;
			    }else{
			    	return false;
			    }

		    } catch (IOException e){
			    if (timesDroped < 1){
		    		timesDroped++;
		    	}else{
		    		return false;
		    	}
			    
			    if (ping(inet)){
			    	return true;
			    }else{
			    	return false;
			    }

		    }
		    return true;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (ping(host)){
				  
				  String subnetaddress = host.getHostAddress();
				  LOG("ping success ip:"+subnetaddress);
				  
				  FileInfo smbinfo = new FileInfo();
				  smbinfo.mFile = new File(mRockExplorer.smb_dir+"/"+subnetaddress);				  
				  smbinfo.mDescription = host.getCanonicalHostName();
				  smbinfo.mDescription = "bogon".equals(smbinfo.mDescription) ? subnetaddress : smbinfo.mDescription;
				  smbinfo.mIcon = res.getDrawable(R.drawable.icon_smb);
				  smbinfo.mIsDir = true;
				  SmbSort(smbinfo);
			  }	
			
			threadOver++;
		}
		
	};
	
	public void searchSmb(){
		String ip = null;
		isSearchOver = false;
		threadOver = 0;
		threadStart = 0;
		AllHostCount = 0;
		ScannedHostCount = 0;
		ArrayList<InetAddress> subnetAdress = new ArrayList<InetAddress>();
		
		Vector<Vector<InetAddress>> vectorlist = getsubnetAddress();
		
		for(int m = 0;m < vectorlist.size();m++){
			Vector<InetAddress> vector = vectorlist.get(m);
			for (int i = 1;i<vector.size()-1;){
				String hostname = new String(); 
				InetAddress pingIp;
		  
				if (searchover()){
					return;
				}
		  
				int AliveThread = threadStart - threadOver;
				LOG("Alive:"+AliveThread+" start:"+threadStart+" over:"+threadOver);
				if (AliveThread < MAX_THREAD){
					pingIp = vector.get(i);
					LOG("IP:"+pingIp.getHostAddress());
					Thread scan = new Thread(new ConnectHost(pingIp));
					scan.setPriority(10);
					scan.start();
					threadStart++;
					ScannedHostCount++;
					i++;
				}else{
					try{
						Thread.sleep(500);
					}catch (Exception e) {
						// TODO: handle exception
					}
				}
		  
			}
		}
		isSearchOver = true;
		return;
	}
	
	public static String mount(String smbpath,FileInfo smbinfo){
		String username = smbinfo.getMUsername();
		String password = smbinfo.getMPassword();
		boolean anonymous = smbinfo.isMAnonymous();
		
		String line = null;
		String allline = null;
		String mountPoint;
		if (smbinfo.getMMountpoint()!=null){
			mountPoint = smbinfo.getMMountpoint();
		}else{
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
			mountPoint = SMB_MOUNTPOINT_ROOT+"/"+sf.format(new Date());
		}
		
		StringBuilder smbpathbuilder = new StringBuilder("\"");
		smbpathbuilder.append(smbpath);
		smbpathbuilder.append("\"");
		
		String newSmbpath = smbpathbuilder.toString();
				
		File shellDir = new File(SHELL_ROOT);
		if(!shellDir.exists()){
			if(!shellDir.mkdirs()){
				allline = res.getString(R.string.shell_error);
				return allline;
			}
		}

		String result = null;
		File shellLog = new File(SHELL_LOG_PATH);
		if (!shellLog.exists()){
			try {
				shellLog.createNewFile();
				shellLog.setExecutable(true);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "mount shell log fail";
				return result;
			}
		}

		if(anonymous){
	        try
	        {
	        	String cifsSetting1 ="echo 0 > /proc/fs/cifs/OplockEnabled";
	        	String cifsSetting2 ="echo 1 > /proc/fs/cifs/LookupCacheEnabled";

	        	String command = "busybox mount -t cifs -o iocharset=utf8,username=guest,uid=1000,gid=1015,file_mode=0775,dir_mode=0775,rw" +
	        			" "+newSmbpath+" "+mountPoint+" > "+SHELL_LOG_PATH+" 2>&1";
	        	LOG("mount command:"+command);
	            String []cmd = {SHELL_HEAD,command};
	            if (!ShellFileWrite(cmd)){
	            	allline = res.getString(R.string.shell_error);
	            	return allline;
	            }
	           
	        }
	        catch (Exception ex)
	        {
	            System.out.println(ex.getMessage());
	            allline = res.getString(R.string.shell_error);
	            return allline;
	        }

		}else {
			try
	        {
				String user = username;
				String pass = password;
				if (username.contains(" ")){
					StringBuilder userbuilder = new  StringBuilder("\"");
					userbuilder.append(username);
					userbuilder.append("\"");
					user = userbuilder.toString();
				}
				if (password.contains(" ")){
					StringBuilder passbuilder = new StringBuilder("\"");
					passbuilder.append(password);
					passbuilder.append("\"");
					pass = passbuilder.toString();
				}
				String cifsSetting1 ="echo 0 > /proc/fs/cifs/OplockEnabled";
	        	String cifsSetting2 ="echo 1 > /proc/fs/cifs/LookupCacheEnabled";
	        	
	        	String command = "busybox mount -t cifs -o iocharset=utf8,username="+user+","+"password="+pass
	        	+",uid=1000,gid=1015,file_mode=0775,dir_mode=0775,rw "+newSmbpath+" "+mountPoint+" > "+SHELL_LOG_PATH+" 2>&1";
	        	LOG("mount command:"+command);
	        	String []cmd = {SHELL_HEAD,command};
		        if (!ShellFileWrite(cmd)){
		        	allline = res.getString(R.string.shell_error);
		        	return allline;
		        }	           
	        }
	        catch (Exception ex)
	        {
	            System.out.println(ex.getMessage());
	            allline = res.getString(R.string.shell_error);
	            return allline;
	        }

		}
		
		if(!creatMountPoint(mountPoint)){
			allline = res.getString(R.string.mountpoint_create_error);
			return allline;
		}
		
		int timeout = 0;
		while(true){
			if (timeout > 2) {
		  		break;
			}
							 
			allline = null;
			SystemProperties.set("ctl.start", "cifsmanager");
			try{
				Thread.sleep(3000);
			}catch(Exception ex){
				Log.e(TAG, "Exception: " + ex.getMessage());
				allline = res.getString(R.string.mount_exception);
				timeout++;
				continue;
			}
		
			String mount_rt = SystemProperties.get("init.svc.cifsmanager", "");
			LOG("mount runtime:"+mount_rt+" timeout:"+timeout);
		
			if(mount_rt != null && mount_rt.equals("running")){
				allline = res.getString(R.string.connect_timeout);
				SystemProperties.set("ctl.stop", "cifsmanager");
				timeout++;
				continue;
			}
		
			if(mount_rt != null && mount_rt.equals("stopped")){
				allline = ShellLogRead();
				if (allline == null){
					break;
				}
			}
			timeout++;
		}

		
		if(allline != null){
			deleteMountPoint(mountPoint);
		}else{
			smbinfo.setMMountpoint(mountPoint);
		}
		return allline;
	}
	
	public static String umount(String mountpoint){
		String line = null;
		String allline = null;
		String mountPoint = mountpoint;
		if (!new File(mountPoint.replace("\"", "")).exists()){
			allline = res.getString(R.string.mountpoint_lost);
			return allline;
		}
		try
		{
	       String command = "busybox umount -fl "+mountPoint+" > "+SHELL_LOG_PATH+" 2>&1";
	       LOG("umount command:"+command);
	       String []cmd = {SHELL_HEAD,command};
		   if (!ShellFileWrite(cmd)){
		       allline = res.getString(R.string.shell_error);
		       return allline;
		   }	           
		}catch (Exception ex){
			System.out.println(ex.getMessage());
	        allline = res.getString(R.string.shell_error);
	        return allline;
	    }
		
		SystemProperties.set("ctl.start", "cifsmanager");
		
		while(true)
		{
			String mount_rt = SystemProperties.get("init.svc.cifsmanager", "");
			if(mount_rt != null && mount_rt.equals("stopped"))
			{
				allline = ShellLogRead();
				break;
			}
			
			try
			{
			      Thread.sleep(1000);
			}catch(Exception ex){
			      Log.e(TAG, "Exception: " + ex.getMessage());
			      allline = res.getString(R.string.mount_exception);
			}
		}
		if(allline == null){
			if(!deleteMountPoint(mountpoint)){
				allline = res.getString(R.string.mountpoint_del_error);;
			}
		}
		return allline;
		
	}
	
	public static boolean creatMountPoint(String path){
		try{
			File root_smb = new File(SMB_MOUNTPOINT_ROOT);
		if(!root_smb.exists()){
			if(!root_smb.mkdirs()){
				return false;	
			}else{
				root_smb.setReadable(true,false);
				root_smb.setExecutable(true, false);
			}
		}
		
		String abpath = new String(path).replace("\"", "");
		
		if(!new File(abpath).exists()){
			if(!new File(abpath).mkdirs()){
				return false;
			}else{
				LOG("creat mount point:"+abpath);
			}
		}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return true;
		
	}
	
	public static boolean deleteMountPoint(String path){
		String abpath = new String(path).replace("\"", "");
		
		if(new File(abpath).exists()){
			if (!new File(abpath).delete()){
				return false;
			}else{
				LOG("delete mount point:"+abpath);
			}
		}		
		return true;		
	}
	
	private static boolean ShellFileWrite(String []cmd){

		File shell = new File(SHELL_PATH);
		if (!shell.exists()){
			try {
				shell.createNewFile();
				shell.setExecutable(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		try {
			BufferedWriter buffwr = new BufferedWriter(new FileWriter(shell));
			for (String str:cmd){
				buffwr.write(str);
				buffwr.newLine();
				buffwr.flush();
			}
			buffwr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;		
	}
	
	private static String ShellLogRead(){
		String result = null;
		File shellLog = new File(SHELL_LOG_PATH);
		if (!shellLog.exists()){
			try {
				shellLog.createNewFile();
				shellLog.setExecutable(true);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "create shell log fail";
				return result;
			}
		}
		
		try {
			BufferedReader buffrd = new BufferedReader(new FileReader(shellLog));
			String str = null;
			while((str=buffrd.readLine())!=null){
				System.out.println(str);
				if(result == null){
					result = str+"\n";
				}else{
					result = result + str + "\n";
				}
			}
			buffrd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "read shell log fail";
			return result;
		}
		return result;
	}
	
	public boolean searchover(){
		return isSearchOver;
	}
	
	public void setSearchOver(boolean isSearchOver){
		this.isSearchOver = isSearchOver;
	}
	
	public synchronized void SmbSort(FileInfo smbinfo){
		LOG("Sort:"+smbinfo.mFile.getPath());

		Message msg = new Message();
		msg.what = MSG_EXPAND_GROUP;
		msg.obj = smbinfo;
		mHandler.sendMessage(msg);
	}
	
	public static ArrayList<String> getMountMsg(){
		String line = null;
		ArrayList<String> strlist = new ArrayList<String>();
		try {
			Process pro = Runtime.getRuntime().exec("mount");
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = br.readLine())!=null){
				strlist.add(line);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		return strlist;
	}
	
	public static boolean judge(String address){
		if(address == null){
			return false;
		}

		String regexp1 = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
		Pattern pattern1 = Pattern.compile(regexp1);
		Matcher matcher1 = pattern1.matcher(address);
		while(matcher1.find()){
			return true;
		}
		return false;
	}

	public int getAllHostCount() {
		return AllHostCount;
	}

	public int getScannedHostCount() {
		return ScannedHostCount;
	}
	
	
}
