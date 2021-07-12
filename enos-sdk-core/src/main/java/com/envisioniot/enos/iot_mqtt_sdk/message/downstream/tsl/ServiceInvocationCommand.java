package com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhensheng.cai on 2018/7/3.
 */
public class ServiceInvocationCommand extends BaseMqttCommand<ServiceInvocationReply> {
    private static final long serialVersionUID = -6852970783309556308L;
    private static Pattern TOPIC_PATTERN = Pattern.compile(ArrivedTopicPattern.SERVICE_INVOKE_COMMAND);
    private static final Pattern METHOD_PATTERN = Pattern.compile("thing\\.service\\.(.*)");

    @Override
    public Class<ServiceInvocationReply> getAnswerType() {
        return ServiceInvocationReply.class;
    }

    @Override
    public Pattern getMatchTopicPattern() {
        return TOPIC_PATTERN;
    }

    @Override
    public Pattern getMethodPattern() {
        return METHOD_PATTERN;
    }

    @Override
    public List<String> match(String topic) {
        //FIXME  topic match conflict with MeasurepointSet & MeasurepointGet
        Matcher matcher = this.getMatchTopicPattern().matcher(topic);
        if (matcher.matches()) {
            String[] groups = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                groups[i] = matcher.group(i + 1);
            }
            List<String> topicArgs = Arrays.asList(groups);
            if (topicArgs.size() == 3 &&
                    ("measurepoint/set".equals(topicArgs.get(2)) ||
                            "measurepoint/get".equals(topicArgs.get(2)))) {
                return null;
            }
            return topicArgs;
        }

        return null;
    }
}
