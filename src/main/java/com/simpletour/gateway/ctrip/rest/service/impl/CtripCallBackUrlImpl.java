package com.simpletour.gateway.ctrip.rest.service.impl;

import com.alibaba.fastjson.JSON;
import com.simpletour.common.utils.JerseyUtil;
import com.simpletour.domain.order.Order;
import com.simpletour.gateway.ctrip.error.CtripOrderError;
import com.simpletour.gateway.ctrip.rest.pojo.VerifyOrderRequest;
import com.simpletour.gateway.ctrip.rest.pojo.VerifyResponse;
import com.simpletour.gateway.ctrip.rest.pojo.bo.CtripOrderBo;
import com.simpletour.gateway.ctrip.rest.pojo.bo.CtripOrderCallBackBo;
import com.simpletour.gateway.ctrip.rest.pojo.type.ResponseHeaderType;
import com.simpletour.gateway.ctrip.rest.pojo.type.orderType.RequestBodyType;
import com.simpletour.gateway.ctrip.rest.service.CtripCallBackUrl;
import com.simpletour.gateway.ctrip.util.StringUtils;
import com.simpletour.gateway.ctrip.util.XMLParseUtil;
import com.simpletour.service.order.IOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * Created by Mario on 2016/2/19.
 */
@Service
public class CtripCallBackUrlImpl implements CtripCallBackUrl {

    @Resource
    private IOrderService orderService;

    @Value("${xiecheng.mp.signkey}")
    private String mpSignKey;

    @Value("${xiecheng.callback.cancelOrder}")
    private String url;

    @Value("${xiecheng.callback.consumeOrder}")
    private String consumeUrl;

    @Override
    public VerifyResponse getCancelOrderCallBack(String request) {
        if (request == null || request.isEmpty()) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.ORDER_ID_NOT_EXISTED));
        }
        //解析请求,用于区分是回调审核成功或是失败
        CtripOrderCallBackBo ctripOrderCallBackBo;
        try {
            ctripOrderCallBackBo = JSON.parseObject(request, CtripOrderCallBackBo.class);
        } catch (Exception e) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.JSON_RESOLVE_FAILED));
        }
        if (ctripOrderCallBackBo == null || ctripOrderCallBackBo.getId() == null || ctripOrderCallBackBo.getType() == null) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.JSON_RESOLVE_FAILED));
        }

        //查询一次该订单信息，获取该订单的信息用于构造数据发送请求
        Optional<Order> orderOptional = orderService.findOrderById(ctripOrderCallBackBo.getId());
        if (!orderOptional.isPresent()) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.ORDER_NULL_BY_ID));
        }

        //构造请求
        VerifyOrderRequest verifyOrderRequest = new VerifyOrderRequest();
        try {
            verifyOrderRequest.buildRequest(orderOptional.get(), mpSignKey, ctripOrderCallBackBo.getType());
        } catch (UnsupportedEncodingException e) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.ORDER_TRANSFER_TO_REQUEST_FAILED));
        }

        //调用请求
        String response = JerseyUtil.getResultForUrl(url, StringUtils.formatXml(XMLParseUtil.convertToXml(verifyOrderRequest)), "xml");
        if (response == null || response.isEmpty()) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.ORDER_CALL_BACK_NULL));
        }
        return XMLParseUtil.convertToJavaBean(response, VerifyResponse.class);
    }

    @Override
    public VerifyResponse getConsumeOrderCallBack(RequestBodyType requestBodyType) throws UnsupportedEncodingException {
        VerifyOrderRequest verifyOrderRequest = new VerifyOrderRequest().buildRequestForConsume(requestBodyType, mpSignKey);
        //调用请求
        String response = JerseyUtil.getResultForUrl(consumeUrl, StringUtils.formatXml(XMLParseUtil.convertToXml(verifyOrderRequest)), "xml");
        if (response == null || response.isEmpty()) {
            return new VerifyResponse(new ResponseHeaderType(CtripOrderError.ORDER_CALL_BACK_NULL));
        }
        return null;
    }
}
