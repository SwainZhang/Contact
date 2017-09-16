package com.example.emery.contact.utils;

import com.example.emery.contact.DataResonseListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * Created by emery on 2017/4/23.
 */

public class HttpUtil {
    public static void post(final String url, final Map<String, String> map, final DataResonseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    //设置请求方式,请求超时信息
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    //设置运行输入,输出:
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    //Post方式不能缓存,需手动设置为false
                    conn.setUseCaches(false);
                    //我们请求的数据:
                    StringBuilder builde = new StringBuilder();
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                        builde.append(entry.getKey() + "=" + value);
                        builde.append("&");
                    }
                    String data = builde.toString();
                    data = data.substring(0, data.lastIndexOf("&"));
                    System.out.println("data==" + data);
                    //这里可以写一些请求头的东东...
                    //获取输出流
                    OutputStream out = conn.getOutputStream();
                    out.write(data.getBytes());
                    out.flush();
                    if (conn.getResponseCode() == 200) {
                        // 获取响应的输入流对象
                        InputStream is = conn.getInputStream();
                        // 创建字节输出流对象
                        ByteArrayOutputStream message = new ByteArrayOutputStream();
                        // 定义读取的长度
                        int len = 0;
                        // 定义缓冲区
                        byte buffer[] = new byte[1024];
                        // 按照缓冲区的大小，循环读取
                        while ((len = is.read(buffer)) != -1) {
                            // 根据读取的长度写入到os对象中
                            message.write(buffer, 0, len);
                        }
                        // 释放资源
                        is.close();
                        message.close();
                        // 返回字符串
                        msg = new String(message.toByteArray());
                        listener.onResponse(msg);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }
}