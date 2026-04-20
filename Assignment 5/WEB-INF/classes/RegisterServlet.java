import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RegisterServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String department = request.getParameter("department");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_db",
                "root",
                "root123"
            );

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO employee(name, email, department) VALUES (?, ?, ?)"
            );

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, department);

            ps.executeUpdate();

            out.println("<h2>Data Saved Successfully!</h2>");

            conn.close();

        } catch (Exception e) {
            out.println(e);
        }
    }
}