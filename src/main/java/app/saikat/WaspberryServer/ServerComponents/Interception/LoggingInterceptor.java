package app.saikat.WaspberryServer.ServerComponents.Interception;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    private final FileWriter fileWriter;
    private SimpleDateFormat formatter;

    public LoggingInterceptor() throws IOException {
        File accessFile = new File("access.log");
        fileWriter = new FileWriter(accessFile, true);

        formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss:SSS");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpServletRequest requestCacheWrapperObject = new ContentCachingRequestWrapper(request);
        requestCacheWrapperObject.getParameterMap();

        StringBuffer logBuilder = new StringBuffer(formatter.format(new Date()));
        logBuilder.append(" ")
                .append(requestCacheWrapperObject.getRemoteAddr() + "(" + requestCacheWrapperObject.getRemoteUser()
                        + ")")
                .append(" ").append(requestCacheWrapperObject.getSession(true).getId()).append(" ")
                .append(requestCacheWrapperObject.getMethod() + ": " + requestCacheWrapperObject.getRequestURI());

        StringBuffer headers = new StringBuffer();
        Enumeration<String> headerKeys = requestCacheWrapperObject.getHeaderNames();
        while (headerKeys.hasMoreElements()) {
            String key = headerKeys.nextElement();
            headers.append(key + ": " + requestCacheWrapperObject.getHeader(key) + ", ");
        }

        synchronized (fileWriter) {
            fileWriter.append(logBuilder.append("\n"));
            fileWriter.append("{ " + headers.substring(0, headers.length() - 2) + " }\n");
            fileWriter.flush();
        }

        return true;
    }
}