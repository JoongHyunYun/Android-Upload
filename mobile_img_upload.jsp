<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.oreilly.servlet.MultipartRequest,com.oreilly.servlet.multipart.DefaultFileRenamePolicy,java.util.*" %>
<%@ page import="net.java.img.util.*,net.java.properties.*,net.java.util.*" %>    
<%
    	request.setCharacterEncoding("UTF-8");

    long time = System.currentTimeMillis();

    ReadProperties pro = new ReadProperties();
    pro.read();
    
    //String folderTypePath = "E:\\img\\"+request.getHeader("Cookie");
    String folderTypePath = pro.getUserPath()+request.getHeader("Cookie")+"\\temp\\pages\\";
    String thumbPath = pro.getUserPath()+request.getHeader("Cookie")+"\\temp\\thumb\\";

    String name="",fileName="",type="",shaid="";
    shaid=request.getHeader("Cookie");
    
    int sizeLimit = 7 * 1024 * 1024 ; 
    MultipartRequest multi = new MultipartRequest(request, folderTypePath, sizeLimit,"UTF-8",new DefaultFileRenamePolicy());
    Enumeration<?> files = multi.getFileNames();

    type=request.getHeader("Type");
    //파일 정보가 있다면
    if(files.hasMoreElements()) {
         name = (String)files.nextElement();
         fileName = multi.getFilesystemName(name);
         
         String typeArray[] = type.split("\\,");
         String fullFilePath = folderTypePath+"\\"+fileName;
         Img_Reform_Mobile.ImageMake_White(fullFilePath, typeArray[0], typeArray[1]);
         Img_Reform_Mobile.resizeimage(fullFilePath, thumbPath);
    }
    
    %>
<%=shaid%><!--반환값 android log Test-->
