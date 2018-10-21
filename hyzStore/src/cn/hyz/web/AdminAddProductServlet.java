package cn.hyz.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import cn.hyz.domain.Category;
import cn.hyz.domain.Product;
import cn.hyz.service.AdminService;
import cn.hyz.utils.CommonsUtils;

public class AdminAddProductServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//收集数据的map
		Map<String,Object> map = new HashMap<String,Object>();
		Product product = new Product();
		try {
			//1.创建磁盘文件项工厂，并设置临时存储文件的大小和位置
			DiskFileItemFactory factory = new DiskFileItemFactory();
			String path = this.getServletContext().getRealPath("temp");
			factory.setRepository(new File(path));
			factory.setSizeThreshold(1024*1024);
			//2.创建文件上传的核心类
			ServletFileUpload upload = new ServletFileUpload(factory);
			//3.设置上传文件的名称和编码
			upload.setHeaderEncoding("UTF-8");
			//4.判断表单是否是文件上传的表单，看看有没有写enctype
			boolean multipartContent = upload.isMultipartContent(request);
			if(multipartContent) {
				//代表是文件上传的表单
				//解析request
				List<FileItem> parseRequest = upload.parseRequest(request);
				if(parseRequest!=null) {
					for(FileItem item : parseRequest) {
						//判断是不是一个普通的表单
						boolean formField = item.isFormField();
						if(formField) {
							//是一个普通的表单
							String fieldName = item.getFieldName();
							String fieldValue = item.getString("UTF-8");
							//存到一个Map中
							map.put(fieldName, fieldValue);
						}else {
							//是上传的文件
							//获得名称
							String fileName = item.getName();
							//获得存储的地址
							String filePath = this.getServletContext().getRealPath("upload");
							//获得读取上传文件的流
							InputStream in = item.getInputStream();
							//获得写入文件的流
							OutputStream out = new FileOutputStream(filePath+"/"+fileName);
							IOUtils.copy(in, out);
							//放入map
							map.put("pimage", "upload/"+fileName);//这儿写一个相对地址比较好
							//删除临时文件
							item.delete();
							//关闭资源
							in.close();
							out.close();
						}
					}
				}
			}
			
			//封装数据
			BeanUtils.populate(product, map);
			//封装不能从表单中获取的数据
			product.setPid(CommonsUtils.getUUID());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date = format.format(new Date());
			Date date1 = format.parse(date);
			product.setPdate(date1);
			product.setPflag(0);
			Category category = new Category();
			category.setCid(map.get("cid").toString());
			product.setCategory(category);
			//将封装好的Product传递给service层
			AdminService service = new AdminService();
			service.saveProduct(product);
		} catch (FileUploadException | IllegalAccessException | InvocationTargetException | ParseException e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}