import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * A minimal servlet that returns the link to the correct
 * version of the game for the BlackBerry handset
 * to download the game OTA.
 */
public class GamePage extends HttpServlet {

  /**
   * Handle the GET request (others are ignored).
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // get the User-Agent to identify the device accessing the page:
    String userAgent = request.getHeader("User-Agent");

    // Prepare the response page:
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    out.println("<html>");
    out.println("<head>");
    out.println("<title>Ladybug Maze download page</title>");
    out.println("</head>");
    out.println("<body bgcolor=\"white\">");

    // get the context to construct the correct URL 
    // for the Jad file, relative to this installation
    String context = request.getContextPath();
    if((userAgent != null) && (userAgent.startsWith("BlackBerry8700"))) {
      out.println("<a href=\"" + context + "/binaries/8700/LadybugMaze.jad\">Ladybug Maze</a> ");
    } else {
      out.println("Sorry, but this game is not available for your handset.");
    }
    out.println("</body>");
    out.println("</html>");
  }
}
