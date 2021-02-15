package de.tubs.cs.ias.asm_test.sql_injection;

import de.tubs.cs.ias.asm_test.sql_injection.attack_cases.CommentLineAttack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SQLChecker {

    public static void checkTaintedString(String json_string) throws IOException {
        JSONObject json_obj = new JSONObject(json_string);
        String sql_string = json_obj.getString("payload");
        JSONArray input_ranges = json_obj.getJSONArray("ranges");
        for (int i = 0; i < input_ranges.length(); i++){
            int start_index = (int) input_ranges.getJSONObject(i).get("start");
            int end_index = (int) input_ranges.getJSONObject(i).get("end");
            String user_input = sql_string.substring(start_index,end_index);
            System.out.println(user_input);
            //checkAttack(user_input);
        }
    }

    public static void printCheck(String json_string) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\I542925\\IdeaProjects\\java-bytecode-tainter-modified\\xyz.txt"));
        out.write(json_string);
        out.close();
    }

    private static void checkAttack(String tainted_string){
        JSONArray attack_results =new JSONArray();
        attack_results.put(CommentLineAttack.checkCommentLineAttack(tainted_string));
    }

    public static void main(String[] args) throws IOException {
        String tainted_json_string = "{\"sink\":\"java/sql/Statement.executeQuery(Ljava/lang/String;)Ljava/sql/ResultSet;\",\"category\":\"unknown\",\"payload\":\"SELECT * FROM employee_table where id = 3;Select * from employee_table;;\",\"ranges\":[{\"start\":40,\"end\":71,\"source\":{\"name\":\"getParameterHttp\",\"id\":5}}],\"stackTrace\":[\"com.sap.extras.sql.postgres.selectQuery(postgres.java:44)\",\"com.example.db.FetchFromDb.doGet(FetchFromDb.java:31)\",\"jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)\",\"jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\",\"jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\",\"java.lang.reflect.Method.invoke(Method.java:566)\",\"de.tubs.cs.ias.asm_test.taintaware.shared.IASReflectionMethodProxy.invoke(IASReflectionMethodProxy.java:145)\",\"org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197)\",\"org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141)\",\"org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106)\",\"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:894)\",\"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)\",\"org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\",\"org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1060)\",\"org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:962)\",\"org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)\",\"org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:898)\",\"javax.servlet.http.HttpServlet.service(HttpServlet.java:626)\",\"org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)\",\"javax.servlet.http.HttpServlet.service(HttpServlet.java:733)\",\"org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)\",\"org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\",\"org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\",\"org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\",\"org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\",\"org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\",\"org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\",\"org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\",\"org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\",\"org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\",\"org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\",\"org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\",\"org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\",\"org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\",\"org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\",\"org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\",\"org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\",\"org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)\",\"org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\",\"org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542)\",\"org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143)\",\"org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\",\"org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\",\"org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)\",\"org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374)\",\"org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\",\"org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:888)\",\"org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1597)\",\"org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\",\"java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)\",\"java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)\",\"org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\",\"java.lang.Thread.run(Thread.java:834)\"]}";

        SQLChecker.checkTaintedString(tainted_json_string);
    }
}
