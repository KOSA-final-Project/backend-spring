package site.hesil.latteve_spring.domains.alarm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.hesil.latteve_spring.domains.alarm.dto.AlarmsResponse;
import site.hesil.latteve_spring.domains.alarm.service.AlarmService;
import site.hesil.latteve_spring.global.security.annotation.AuthMemberId;

/**
 * packageName    : site.hesil.latteve_spring.domains.alarm.controller
 * fileName       : AlarmController
 * author         : Yeong-Huns
 * date           : 2024-09-09
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-09-09        Yeong-Huns       최초 생성
 */
@Log4j2
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/notifications")
    public ResponseEntity<AlarmsResponse> getNotifications(@AuthMemberId Long memberId) {
        AlarmsResponse alarmsResponse = alarmService.getNotifications(memberId);
        return ResponseEntity.ok(alarmsResponse);
    }
}
