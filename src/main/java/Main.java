import com.twilio.sdk.TwilioRestException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends HttpServlet {
    //For debugging purposes.
    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    //Function that does the connecting. This is always called on any clal to the API
    private static Connection getConnection(HttpServletResponse response) throws IOException {
        try {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (URISyntaxException | SQLException e) {
            response.setStatus(Constants.INTERNAL_SERVER_ERROR);
            //response.getWriter().print(Constants.DB_CONNECTION_FAIL);
            return null;
        }
    }

    //Main function
    public static void main(String[] args) throws Exception {
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new Main()), "/*");
        server.start();
        server.join();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        Connection connection = getConnection(response);
        try {
            //If a GET HTTP request is sent, this function is called.
            if (connection != null) {
                //if a connection is successfully made, parse the URI and call functions based on the parsed URI
                String path = request.getRequestURI();
                String[] pathPieces = path.split("/");
                if (pathPieces[1].equals("totalCallCnt")) {
                    TotalCallCnt.getTotalCallCnt(request, response, connection);
                } else if (pathPieces[1].equals("history")) {
                    String from = request.getParameter("from");
                    String to = request.getParameter("to");
                    String mode = request.getParameter("mode");
                    long timestamp;

                    try {
                        timestamp = Long.parseLong(request.getParameter("timestamp"));
                    } catch(NumberFormatException e) {
                        response.setStatus(Constants.BAD_REQUEST);
                        response.getWriter().print(getStackTrace(e));
                        return;
                    }

                    if (mode == null) {
                        mode = Constants.LOAD_MODE;
                    }

                    if (from != null)
                        GetHistory.getHistory(request, response, connection, from,
                            (to != null ? to : ""),  mode, timestamp);
                }
            }
        } catch (Exception e) {
            response.setStatus(Constants.NOT_FOUND);
            response.getWriter().print(getStackTrace(e));
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //If a POST HTTP request is sent, this function is called.
        // Get request body into string
        StringBuilder requestBody = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            response.setStatus(Constants.INTERNAL_SERVER_ERROR);
            return;
        }

        // Get connection to DB
        Connection connection = getConnection(response);
        if (connection != null) {
            // Business logic
            //If the connection is successfully made, parse URI and call functions based on that
            try {
                JSONObject jsonObject = new JSONObject(requestBody.toString());
                String path = request.getRequestURI();
                String[] pathPieces = path.split("/");
                if (pathPieces[1].equals("call")) {
                    MakeCall.makeCall(request, response, connection, jsonObject);
                } else if (pathPieces[1].equals("createAccount")) {
                    CreateAccount.createAccount(request, response, connection, jsonObject);
                } else if (pathPieces[1].equals("activateAccount")) {
                    ActivateAccount.activateAccount(request, response, connection, jsonObject);
                } else if (pathPieces[1].equals("login")) {
                    Login.login(request, response, connection, jsonObject);
                } else
                    response.setStatus(Constants.NOT_FOUND);
            } catch (JSONException e) {
                response.setStatus(Constants.BAD_REQUEST);
                response.getWriter().print(getStackTrace(e));
            } catch (TwilioRestException e) {
                response.setStatus(Constants.INTERNAL_SERVER_ERROR);
                response.getWriter().print(Constants.TWILIO_ERROR);
            } finally {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
