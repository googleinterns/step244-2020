package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.ThreadManager;

public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ExecutorService executor = Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory());
        List<Callable<String>> lst = new ArrayList<>();
        for (int i = 0; i < 150; ++i) {
            Callable<String> callable = () -> {
                //TimeUnit.SECONDS.sleep(2);
                return "dai";
            };
            lst.add(callable);
        }
        try {
            List<Future<String>> futures = executor.invokeAll(lst);
            for (Future<String> iterator : futures) {
                resp.getWriter().println(iterator.get());
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            return ;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
