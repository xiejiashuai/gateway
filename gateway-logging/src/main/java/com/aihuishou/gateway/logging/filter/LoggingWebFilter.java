//package com.aihuishou.gateway.logging.filter;
//
//import brave.Tracing;
//import com.aihuishou.common.util.DateUtil;
//import com.aihuishou.common.util.StringUtil;
//import com.aihuishou.common.util.ThreadLocalUtils;
//import com.aihuishou.gateway.logging.util.LoggingContextHolder;
//import com.google.common.collect.Maps;
//import org.apache.commons.collections.MapUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.PathMatcher;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import org.springframework.web.util.ContentCachingRequestWrapper;
//import org.springframework.web.util.ContentCachingResponseWrapper;
//import org.springframework.web.util.UrlPathHelper;
//import reactor.core.publisher.Mono;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.*;
//
///**
// * 日志过滤器
// * 打印 access log
// *
// * @author weichao.li (liweichao0102@gmail.com)
// * @date 2018/8/19
// */
//public class LoggingWebFilter implements WebFilter {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingWebFilter.class);
//
//    private PathMatcher pathMatcher = new AntPathMatcher();
//
//    private final List<String> excludePatterns;
//
//    private final Set<String> extraHeaders;
//
//    public LoggingWebFilter(List<String> excludePatterns, Set<String> extraHeaders) {
//        this.excludePatterns = excludePatterns;
//        this.extraHeaders = extraHeaders;
//    }
//
//
//    private static StringBuilder generateResultLogger(Map<String, Map<String, String>> logMap) {
//        StringBuilder resultStr = new StringBuilder();
//        Map<String, String> requestMap = logMap.get(LoggerConst.REQUEST_IDENTITY);
//        Map<String, String> responseMap = logMap.get(LoggerConst.RESPONSE_IDENTITY);
//        //遍历request key, 并将相应的值写入StringBuilder中
//        resultStr.append("\n");
//        for (String requestKey : LoggerConst.REQUEST_KEY_LIST) {
//            String requestValue = requestMap.get(requestKey);
//            if (StringUtils.isBlank(requestValue)) {
//                requestValue = LoggerConst.VALUE_DEFAULT;
//            }
//            appendKeyValue(resultStr, requestKey, requestValue, LoggerConst.REQUEST_PREFIX);
//        }
//        resultStr.append("\n");
//        //遍历response key, 并将相应的值写入StringBuilder中
//        for (String responseKey : LoggerConst.RESPONSE_KEY_LIST) {
//            String responseValue = responseMap.get(responseKey);
//            if (StringUtils.isBlank(responseValue)) {
//                responseValue = LoggerConst.VALUE_DEFAULT;
//            }
//            appendKeyValue(resultStr, responseKey, responseValue, LoggerConst.RESPONSE_PREFIX);
//        }
//
//        return resultStr;
//    }
//
//    private static void appendKeyValue(StringBuilder sb, String key, String value, String prefix) {
//        sb.append(prefix).append(key).append(LoggerConst.KEY_VALUE_SEPERATOR).append(value).append("\n");
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//
//        // pre
//        LoggingContextHolder.setRequesTtime(System.currentTimeMillis());
//
//
//        chain.filter(exchange).then();
//
//        try {
//           chain.filter(exchange);
//        } finally {
//            try {
//                //过滤不打印日志的 url
//
//                String uri = exchange.getRequest().getPath().toString();
//                boolean skip = false;
//                for (String excludePattern : excludePatterns) {
//                    if (pathMatcher.match(excludePattern, uri)) {
//                        skip = true;
//                    }
//                }
//                if (!skip) {
//
//
//                    Map<String, Map<String, String>> logMap = Maps.newHashMap();
//
//                    Map<String, String> requestMap = Maps.newHashMap();
//                    Map<String, String> responseMap = Maps.newHashMap();
//
//                    Map<String, String> extraParamMap = Maps.newConcurrentMap();
//                    /*
//                     * request 日志处理
//                     */
//                    Date requestTime = new Date(LoggingContextHolder.getRequesTtime());
//                    String bodyParam = new String(, requestWrapper.getCharacterEncoding());
//                    extraParamMap.putAll(ExtraParamUtils.getAllAndRemove());
//                    if (MapUtils.isNotEmpty(requestWrapper.getParameterMap())) {
//                        extraParamMap.put(LoggerConst.REQUEST_KEY_FORM_PARAM, StringUtil.toJSONString(request.getParameterMap()));
//                    }
//                    Enumeration<String> entries = request.getHeaderNames();
//                    while (entries.hasMoreElements()) {
//                        String headerName = entries.nextElement();
//                        for (String extraHeader : extraHeaders) {
//                            if (StringUtils.equalsIgnoreCase(extraHeader, headerName)) {
//                                extraParamMap.put(headerName, StringUtil.toJSONString(requestWrapper.getHeader(headerName)));
//                                break;
//                            }
//                        }
//                        requestMap.put(headerName.toLowerCase(), requestWrapper.getHeader(headerName));
//                    }
//                    /*
//                     * response 日志处理
//                     */
//                    String responseData = IOUtils.toString(responseWrapper.getContentInputStream(), responseWrapper.getCharacterEncoding());
//                    /*
//                     * 日志处理
//                     */
//                    requestMap.put(LoggerConst.REQUEST_KEY_REQUEST_TIME, LoggerConst.DATE_FORMAT.format(requestTime));
//                    requestMap.put(LoggerConst.REQUEST_KEY_URL, request.getRequestURL().toString());
//                    requestMap.put(LoggerConst.REQUEST_KEY_HTTP_METHOD, request.getMethod());
//                    requestMap.put(LoggerConst.REQUEST_KEY_BODY_PARAM, StringUtils.isNotBlank(bodyParam) ? org.springframework.util.StringUtils.trimAllWhitespace(bodyParam) : LoggerConst.VALUE_DEFAULT);
//                    requestMap.put(LoggerConst.REQUEST_KEY_EXTRA_PARAM, StringUtil.toJSONString(extraParamMap));
//
//                    Date responseDate = new Date();
//                    responseMap.put(LoggerConst.RESPONSE_KEY_RESPONSE_TIME, LoggerConst.DATE_FORMAT.format(responseDate));
//                    responseMap.put(LoggerConst.RESPONSE_KEY_TAKE_TIME, String.valueOf(DateUtil.interval(responseDate, requestTime)));
//                    responseMap.put(LoggerConst.RESPONSE_KEY_HTTP_CODE, String.valueOf(responseWrapper.getStatus()));
//                    responseMap.put(LoggerConst.RESPONSE_KEY_RESPONSE_DATA, StringUtils.isNotBlank(responseData) ? StringUtil.toJSONString(responseData) : "");
//                    responseMap.put(LoggerConst.RESPONSE_KEY_CONTENT_TYPE, responseWrapper.getContentType());
//
//                    //组装request和response
//
//
//                    logMap.put(LoggerConst.REQUEST_IDENTITY, requestMap);
//                    logMap.put(LoggerConst.RESPONSE_IDENTITY, responseMap);
//                    //组装request和response
//
//                    StringBuilder logResult = generateResultLogger(logMap);
//                    LOGGER.info(logResult.toString());
//                }
//            } catch (Exception ignored) {
//            } finally {
//                ThreadLocalUtils.clear();
//                responseWrapper.copyBodyToResponse();
//                LoggerContextHolder.reset();
//            }
//        }
//
//        return null;
//    }
//}