package com.ruoyi.framework.aspectj;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Aspect
@Component
@Order(100)
public class LogConsoleAspect {
    private long start;
    private static final String LOG_PREFIX = "====>";

    /**
     * 定义一个切点，用于匹配所有带有 HTTP 方法注解的方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void httpMappingPointcut() {
        // 该方法体为空，仅作为定义切点使用
    }

    /**
     * 环绕通知
     */
    @Around("httpMappingPointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 前置操作
        this.doBeforte(proceedingJoinPoint);
        Object proceed = null;
        try {
            proceed = proceedingJoinPoint.proceed();
        } finally {
            this.doAfter(proceed);
        }
        return proceed;
    }

    /**
     * 前置通知
     */
    private void doBeforte(ProceedingJoinPoint proceedingJoinPoint) {
        start = System.currentTimeMillis();
        log.info("============================>开始");
        ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ra != null) {
            HttpServletRequest request = ra.getRequest();
            String userName = null;
            String method = request.getMethod();
            String requestURI = request.getRequestURI();
            String hostport = request.getRemoteHost() + ":" + request.getRemotePort();
            log.info(LOG_PREFIX + "[{}] [{}]{} [{}]", userName, method, requestURI, hostport);
        }

        // 输出请求参数
        Object[] args = proceedingJoinPoint.getArgs();
        if (ArrayUtil.isNotEmpty(args)) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (isNotOutputAble(arg)) {
                    if (log.isInfoEnabled())
                        log.info(LOG_PREFIX + "请求参数【{}】：不支持的数据类型：{}", i, arg.getClass().getSimpleName());
                } else {
                    if (log.isInfoEnabled()) log.info(LOG_PREFIX + "请求参数【{}】：{}", i, this.getArg(arg, 5000));
                }
            }
        }

    }

    /**
     * 后置通知-记录耗时
     */
    private void doAfter(Object proceed) {
        try {
            if (log.isInfoEnabled()) log.info(LOG_PREFIX + "请求结果：{}", this.getArg(proceed, 1000));
            long cost = System.currentTimeMillis() - start;
            log.info("<============================结束，耗时{}ms", cost);
        } catch (Exception e) {
            log.error(LOG_PREFIX + "统一日志打印出错：{}", e.getMessage());
        }
    }

    /**
     * 判断是否支持输出
     *
     * @return true 表示不能输出，false 表示可输出的类型
     */
    private static boolean isNotOutputAble(Object arg) {
        return arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof MultipartFile
                || arg instanceof BindingResult;
    }

    /**
     * 打印参数拼装
     */
    private String getArg(Object proceed, int maxLength) {
        if (proceed == null) {
            return "";
        }
        if (proceed instanceof String) {
            return (String) proceed;
        }
        String s = JSONUtil.toJsonStr(proceed);
        if (maxLength <= 1000) maxLength = 1000;
        if (StrUtil.isNotEmpty(s) && s.length() > maxLength) {
            return s.substring(0, maxLength) + "[总长度：" + s.length() + "]";// 控制最长输出
        }
        return s;
    }
}
