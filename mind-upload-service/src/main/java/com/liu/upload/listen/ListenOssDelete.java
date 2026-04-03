package com.liu.upload.listen;

import com.liu.common.common.constant.MqConstant;
import com.liu.common.untils.AliyunOssUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListenOssDelete {

    private final AliyunOssUtil aliyunOssUtil;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_OSS_DELETE)
    public void deleteDocumentOssDelete(String url, Channel channel, Message message) throws IOException {
        String fileUrl = url.replaceFirst("^https?://.*?\\.aliyuncs\\.com/", "");
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到删除oss文件消息: {}", fileUrl);
            aliyunOssUtil.deleteFile(fileUrl);
            log.info("删除oss文件成功: {}", fileUrl);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("删除oss文件失败: {}", fileUrl, e);
            channel.basicNack(tag, false, false);
        }
    }

}
