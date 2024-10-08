package site.hesil.latteve_spring.global.rabbitMQ.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : site.hesil.latteve_spring.global.rabbitMQ.enumerate
 * fileName       : RabbitMQ
 * author         : Yeong-Huns
 * date           : 2024-09-03
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-09-03        Yeong-Huns       최초 생성
 * 2024-09-14        Heeseon          프로젝트 관련 메시지 추가
 */
@Getter
@RequiredArgsConstructor
public enum MQExchange {
    DIRECT_MEMBER("memberDirectExchange"),
    DEAD_LETTER("deadLetterExchange"),
    TOPIC_MEMBER("topicMemberExchange"),
    ALARM("alarmExchange"),


    DIRECT_PROJECT("projectDirectExchange"),
    TOPIC_PROJECT("topicProjectExchange"),

    DIRECT_PROJECT_LIKE("projectLikeDirectExchange"),
    TOPIC_PROJECT_LIKE("topicProjectLikeExchange"),

    DIRECT_PROJECT_MEMBER("projectMemberDirectExchange"),
    TOPIC_PROJECT_MEMBER("topicProjectMemberExchange"),

    ;


    private final String exchange;
}
