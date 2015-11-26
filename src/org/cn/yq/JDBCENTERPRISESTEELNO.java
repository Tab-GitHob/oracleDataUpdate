package org.cn.yq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业钢码编号（唯一约束，与《气瓶标签基本信息管理表》同,好像重复了）
 * @author yq
 *
 */
public class JDBCENTERPRISESTEELNO {

	private static String url = "jdbc:oracle:thin:@localhost:1521:orcl";

	private static String user = "bsims";// system为登陆oracle数据库的用户名

	private static String password = "bsims";// manager为用户名system的密码

	// 连接数据库的方法
	public static Connection getConnection() {
		try {
			// 初始化驱动包
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 根据数据库连接字符，名称，密码给conn赋值
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	// 测试能否与oracle数据库连接成功
	public static void main(String[] args) throws SQLException {

		List<Obj> list = query();

		Connection conn = getConnection();
		Statement st = (Statement) conn.createStatement();

		for (Obj o : list) {
			int id = o.id;
			String str = o.no;
			if (str == null || "".equals(str)) {
				System.out.println("气瓶ID：（" + o.id + "）出厂编号为空！");
				continue;
			}
			String result = getManufacturerNo(str);

			System.out.println("气瓶ID：（" + o.id + "）原始出厂编号为：（" + str + "）修改后为：（" + result + "）");

			if (!result.equals(str)) {
				int i = 0;
				update(id, result, conn, st ,i);
			}
			else
				System.out.println("气瓶ID：（" + o.id + "）出厂编号无特殊符号，不修改！");
		}
		
		st.close();
		conn.close();
		
	}

	/**
	 * 字符过滤
	 * @param str
	 * @return
	 */
	public static String getManufacturerNo(String str) {
		if (str.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*", "").length() == 0) {
			return str;
		}

		StringBuffer sf = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			String k = str.substring(i, i + 1);
			if (k.equals("*"))
				sf.append(k);
			else if (k.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*", "").length() == 0)
				sf.append(k);
		}
		System.out.println(sf.toString());
		return sf.toString();
	}

	/**
	 * 更新符合要求的记录，并返回更新的记录数目
	 * @param id
	 * @param str
	 * @param conn
	 * @param st
	 */
	public static void update(int id, String str, Connection conn, Statement st ,int i) {
		try {
			if(str == null || "".equals(str))
				str = "重复";
			
			String sql = "update bs_bottleinfo set ENTERPRISESTEELNO = '" + str + "' where BOTTLEID = " + id;// 更新数据的sql语句

			int count = st.executeUpdate(sql);// 执行更新操作的sql语句，返回更新数据的个数

			System.out.println("表中更新 " + count + " 条数据"); // 输出更新操作的处理结果

		} catch (Exception e) {
			
			String message = e.getMessage();
			if(message.indexOf("违反唯一约束条件") > 0) { // 当前需要修改的数据有重复，需在结尾追加 *
				i ++;
				int index = str.indexOf("重复");
				if(index >= 0)
					str = str.substring(0, index);
				
				str += "重复" + i;
				update(id, str , conn, st , i);
			} else 
				System.out.println("更新数据失败");
		}
	}

	/* 查询数据库，输出符合要求的记录的情况 */
	public static List<Obj> query() {

		try {
			String sql = "select i.bottleid,ENTERPRISESTEELNO from bs_bottleinfo i"; // 查询数据的sql语句

			Connection conn = getConnection(); // 同样先要获取连接，即连接到数据库
			Statement st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量

			List<Obj> list = new ArrayList<Obj>();
			ResultSet rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集
			System.out.println("最后的查询结果为：");
			while (rs.next()) { // 判断是否还有下一个数据

				// 根据字段名获取相应的值
				int bottleid = rs.getInt("bottleid");
				String manufacturerno = rs.getString("ENTERPRISESTEELNO");

				Obj o = new Obj(bottleid, manufacturerno);
				list.add(o);
			}
			st.close();
			conn.close(); // 关闭数据库连接

			System.out.println(list.size());

			return list;
		} catch (Exception e) {
			System.out.println("查询数据失败");
		}
		return null;
	}

	public static class Obj {
		int id;
		String no;

		public Obj(int id, String no) {
			this.id = id;
			this.no = no;
		}

		public Obj() {
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getNo() {
			return no;
		}

		public void setNo(String no) {
			this.no = no;
		}

	}
}
