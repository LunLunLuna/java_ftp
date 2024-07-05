package com.zenithst.common.util.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * <pre>
 * FTP connection 처리 공통 유틸
 *
 * @author  ZenithST
 * @since   2024.05.10
 * @version 1.0
 * </pre>
 */
public class FTPClients {
	Logger logger = LoggerFactory.getLogger(getClass());

	// FTP 설정
	private FTPClient ftpClient;
	
	public FTPClients() {
		this.ftpClient = new FTPClient();
		
		// 한글 설정
		this.ftpClient.setControlEncoding("UTF-8");
		
		// LOG에 주고받은 명령 출력해주는 설정
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
		
	}
	
	/**
	 * FTP 연결
     * @param serverIp		접속 아이피
     * @param serverPort	접속 포트 번호
     * @param userId		사용자 아이디
     * @param userPwd		비밀번호
	 * */
	public void ftpConnet(String serverIp, int serverPort, String userId, String userPwd) throws Exception {
		try {
			// FTP 연결
			ftpClient.connect("server ip", 21);	//server ip, port
			
			// FTP 서버에 정상적으로 연결되었는지 확인
	        int reply = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(reply)) {
	        	ftpClient.disconnect();
	        	throw new Exception("FTPClient :: server connection failed");
	        }
	        
	        // FTP 로그인이 정상적으로 되는지 확인
	        if(!ftpClient.login("userid", "password")) {
	        	ftpClient.logout();
	        	throw new Exception("FTPClient :: server login failed");
	        }
	        
	        // FTP 로그인
	        ftpClient.setSoTimeout(1000);						// 시간 설정
			ftpClient.login("userid", "password");				// 로그인
			ftpClient.enterLocalPassiveMode();					// Active 모드 설정
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);		// type 설정
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("FTPClient :: server connection failed");
		}
        
	}
	
	/**
	 * FTP 연결 종료
	 * */
	public void ftpClose() throws Exception {
		try {
			if(ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("FTPClient :: server disconnection failed");
		}
	}
	
	/**
	 * FTP 파일 업로드(전송)
     * @param	remoteDir		업로드할 서버 폴더 경로
	 * @param	localFilePath	로컬 파일 경로(파일명 포함)
	 * @return	boolean
	 * */
	public boolean ftpUpload(String remoteDir, String localFilePath) throws Exception {
		boolean result = false;
		try {

			ftpClient.changeWorkingDirectory(localFilePath);	// 파일 업로드 경로 설정
			
			// 파일 업로드 경로 생성
			if(!ftpClient.changeWorkingDirectory(localFilePath)) {
				ftpClient.makeDirectory(localFilePath);
				ftpClient.changeWorkingDirectory(localFilePath);
			}
			
			FileInputStream fileInputStream = new FileInputStream(localFilePath);
			ftpClient.storeFile(remoteDir, fileInputStream);
			
			if(!ftpClient.storeFile(remoteDir, fileInputStream)) {
				throw new Exception("FTPClient :: server upload failed");
			}
			
			result = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage().indexOf("not open") != -1) {
				throw new Exception("FTPClient :: server connection failed");
			}
		}
		return result;
	}

	/**
	 * FTP 파일 저장
     * @param	remoteDir		서버 폴더 경로
	 * @param	localFilePath	다운로드할 로컬 파일 경로(파일명 포함)
	 * @return	boolean
	 * */
	public boolean ftpDownload(String remoteDir, String localFilePath) throws Exception {
		boolean result = false;
		try {
			
			ftpClient.changeWorkingDirectory(localFilePath);	// 파일 저장 경로 설정
			
			// 파일 저장 경로 생성
			if(!ftpClient.changeWorkingDirectory(localFilePath)) {
				ftpClient.makeDirectory(localFilePath);
				ftpClient.changeWorkingDirectory(localFilePath);
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream(localFilePath);
			ftpClient.retrieveFile(remoteDir, fileOutputStream);
			
			if(!ftpClient.retrieveFile(remoteDir, fileOutputStream)) {
				throw new Exception("FTPClient :: server downupload failed");
			}
			
			result = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage().indexOf("not open") != -1) {
				throw new Exception("FTPClient :: server connection failed");
			}
		}
		return result;
	}
	
	/**
     * FTP 파일 목록 조회
     * @param	remotePath	서버 파일 경로
     * @return	ArrayList	조회한 파일명
     */
	public ArrayList<String> getFileList(String remotePath) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		
		try {
			ftpClient.changeWorkingDirectory(remotePath); // 목록 조회할 파일 경로
			
			// 파일정보
			for(FTPFile files : ftpClient.listFiles()) {
				logger.info("file dir ==> "+files.isDirectory());
				logger.info("file name ==> "+files.getName());
				result.add(files.getName());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage().indexOf("not open") != -1) {
				throw new Exception("FTPClient :: server connection failed");
			}
		}
		
		return result;
	}
}
