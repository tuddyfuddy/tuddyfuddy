package com.heejuk.tuddyfuddy.authservice.circuit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CircuitBreakerEventConsumer {

    // 서킷브레이커 상태 변경 이벤트 처리
    @EventListener(value = CircuitBreakerOnStateTransitionEvent.class)
    public void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.info("서킷브레이커 {} 상태 변경: {} -> {}",
            event.getCircuitBreakerName(),
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState());

        // 상태가 OPEN으로 변경된 경우 알림 발송 등 추가 작업 가능
        if (event.getStateTransition().getToState() == State.OPEN) {
            notifyServiceFailure(event.getCircuitBreakerName());
        }
    }

    // 서킷브레이커 에러 이벤트 처리
    @EventListener(value = CircuitBreakerOnErrorEvent.class)
    public void onError(CircuitBreakerOnErrorEvent event) {
        log.error("서킷브레이커 {} 에러 발생: {} - {}",
            event.getCircuitBreakerName(),
            event.getThrowable().getClass().getSimpleName(),
            event.getThrowable().getMessage());
    }

    // 서비스 장애 알림 메소드
    private void notifyServiceFailure(String serviceName) {
        log.warn("서비스 {} 장애 감지 - 관리자 알림 필요", serviceName);
        // 슬랙, MM 등 알림 로직 구현
    }
}