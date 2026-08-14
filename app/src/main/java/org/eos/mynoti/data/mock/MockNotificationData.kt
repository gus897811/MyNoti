package org.eos.mynoti.data.mock

import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.Notification
import org.eos.mynoti.domain.model.NotificationAction
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

object MockNotificationData {

    fun create(now: LocalDateTime = LocalDateTime.now()): List<Notification> = listOf(
        Notification(
            id = 1,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "운영체제 과제 2 제출",
            content = "운영체제 2차 과제 '프로세스 스케줄링 시뮬레이터' 제출 마감이 내일 23:59입니다. 지각 제출은 감점되니 기한 내 제출해 주세요.",
            summary = "내일 23:59까지 과제 2를 제출해야 합니다.",
            receivedAt = now.minusHours(1).minusMinutes(12),
            isImportant = true,
            type = NotificationType.ASSIGNMENT,
            remindAt = now.plusDays(1).withHour(20).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 101, title = "운영체제 과제 2 제출")
            )
        ),
        Notification(
            id = 2,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "자료구조 강의 공지",
            content = "'Data Structures' 주차 공지가 등록되었습니다. 다음 주 실습은 해시 테이블 구현이며, 수업 전 강의노트를 미리 읽어 오세요.",
            summary = "자료구조 다음 주 실습: 해시 테이블. 강의노트 사전 학습 권장.",
            receivedAt = now.minusHours(3).minusMinutes(20),
            isImportant = false,
            type = NotificationType.CLASS,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 3,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "새로운 과제가 등록되었습니다",
            content = "알고리즘 과목의 새 과제 '최단 경로 알고리즘 비교'가 등록되었습니다. 제출 기한은 다음 주 금요일 23:59입니다.",
            summary = "알고리즘 새 과제 등록. 제출 기한은 다음 주 금요일 23:59입니다.",
            receivedAt = now.minusHours(5),
            isImportant = true,
            type = NotificationType.ASSIGNMENT,
            remindAt = now.plusDays(4).withHour(21).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 103, title = "최단 경로 알고리즘 비교 과제 제출")
            )
        ),
        Notification(
            id = 4,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "국가장학금 신청",
            content = "2026학년도 2학기 국가장학금 2차 신청과 관련하여 다음과 같이 안내하오니 기간 내 신청해 주시기 바랍니다. 신청 기간은 8월 20일부터 9월 10일까지이며, 학자금 지원 구간 산정에 시간이 소요되니 서둘러 신청해 주세요.",
            summary = "국가장학금 2차 신청이 9월 10일까지 진행됩니다.",
            receivedAt = now.minusHours(6).minusMinutes(40),
            isImportant = true,
            type = NotificationType.ETC,
            remindAt = now.plusDays(2).withHour(12).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 104, title = "국가장학금 2차 신청")
            )
        ),
        Notification(
            id = 5,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "학사 공지: 계절학기 성적 정정",
            content = "여름 계절학기 성적 정정 기간은 8월 14일부터 8월 16일 18:00까지입니다. 정정 신청은 포털 학사 메뉴에서 가능합니다.",
            summary = "계절학기 성적 정정 8/14–8/16 18:00, 포털에서 신청.",
            receivedAt = now.minusHours(8),
            isImportant = false,
            type = NotificationType.CLASS,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 6,
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            title = "캡스톤 팀플방",
            content = "민준: 내일 도서관 스터디룸 3시로 예약했어요. 발표 자료 초안은 오늘 밤까지 공유 부탁드려요!",
            summary = "내일 3시 도서관 팀플. 발표 초안을 오늘 밤까지 공유해야 합니다.",
            receivedAt = now.minusHours(2).minusMinutes(5),
            isImportant = true,
            type = NotificationType.COMMUNICATION,
            remindAt = now.withHour(22).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 106, title = "발표 자료 초안 공유"),
                NotificationAction(id = 107, title = "도서관 스터디룸 3시 참석")
            )
        ),
        Notification(
            id = 7,
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            title = "수아",
            content = "오늘 점심 학식 갈 사람? 12시 반에 학생회관 앞에서 만나자",
            summary = null,
            receivedAt = now.minusHours(4).minusMinutes(15),
            isImportant = false,
            type = NotificationType.COMMUNICATION,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 8,
            appName = "신한카드",
            appPackageName = AppPackages.SHINHAN_CARD,
            title = "카드 결제 승인",
            content = "신한카드(1234) 12,400원 승인. 스타벅스 캠퍼스점. 일시불.",
            summary = "스타벅스 캠퍼스점 12,400원 결제.",
            receivedAt = now.minusMinutes(45),
            isImportant = false,
            type = NotificationType.FINANCIAL,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 9,
            appName = "카카오뱅크",
            appPackageName = AppPackages.KAKAOBANK,
            title = "입금",
            content = "카카오뱅크 입금 500,000원. 보낸 분: 부모님. 잔액 1,284,200원.",
            summary = "부모님으로부터 50만원 입금. 잔액 128만 원.",
            receivedAt = now.minusHours(7).minusMinutes(10),
            isImportant = false,
            type = NotificationType.FINANCIAL,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 10,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "수강 신청 확인 안내",
            content = "2학기 수강 신청 내역을 확인해 주세요. 폐강 과목은 8월 18일까지 대체 과목 신청이 가능합니다. 장바구니에 담아 둔 전공 선택 과목의 잔여 좌석을 확인하세요.",
            summary = null,
            receivedAt = yesterday(now).withHour(16).withMinute(20),
            isImportant = true,
            type = NotificationType.CLASS,
            remindAt = now.plusDays(3).withHour(10).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 110, title = "폐강 과목 대체 수강 신청 확인")
            )
        ),
        Notification(
            id = 11,
            appName = "LearningX",
            appPackageName = AppPackages.LEARNING_X,
            title = "운영체제 강의 자료 업로드",
            content = "3주차 강의 자료와 실습 코드가 업로드되었습니다. 가상 메모리 챕터를 수업 전에 읽어 오면 실습이 수월합니다.",
            summary = "운영체제 3주차 자료 업로드. 가상 메모리 사전 학습 권장.",
            receivedAt = yesterday(now).withHour(11).withMinute(5),
            isImportant = false,
            type = NotificationType.CLASS,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 12,
            appName = "카카오톡",
            appPackageName = AppPackages.KAKAOTALK,
            title = "동아리 운영진방",
            content = "지현: 이번 주 금요 모임 장소가 학생회관에서 카페로 바뀌었어요. 출석 체크 잊지 마세요!",
            summary = "금요 모임 장소 카페로 변경. 출석 체크 필요.",
            receivedAt = yesterday(now).withHour(19).withMinute(42),
            isImportant = false,
            type = NotificationType.COMMUNICATION,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 13,
            appName = "신한카드",
            appPackageName = AppPackages.SHINHAN_CARD,
            title = "카드 결제 승인",
            content = "신한카드(1234) 8,700원 승인. 교내 서점. 일시불.",
            summary = null,
            receivedAt = now.minusDays(2).withHour(13).withMinute(8),
            isImportant = false,
            type = NotificationType.FINANCIAL,
            remindAt = null,
            isReminded = false
        ),
        Notification(
            id = 14,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "도서관 좌석 예약 마감 임박",
            content = "예약한 제2열람실 좌석이 20분 뒤 자동 취소됩니다. 입실 처리를 완료해 주세요.",
            summary = "도서관 좌석 20분 내 입실 필요. 미입실 시 자동 취소.",
            receivedAt = now.minusDays(3).withHour(9).withMinute(50),
            isImportant = false,
            type = NotificationType.ETC,
            remindAt = null,
            isReminded = true
        ),
        Notification(
            id = 15,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "캡스톤 최종 보고서 제출",
            content = "캡스톤디자인 최종 보고서를 8월 20일 18:00까지 포털에 제출해 주세요. 미제출 시 성적이 부여되지 않습니다.",
            summary = "캡스톤 최종 보고서 8월 20일 18:00 제출.",
            receivedAt = now.minusHours(9),
            isImportant = true,
            type = NotificationType.ASSIGNMENT,
            remindAt = now.plusDays(6).withHour(17).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 115, title = "캡스톤 최종 보고서 제출")
            )
        ),
        Notification(
            id = 16,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "등록금 납부 안내",
            content = "2026학년도 2학기 등록금 납부 기간은 8월 25일부터 8월 29일입니다. 가상계좌로 납부해 주세요.",
            summary = "2학기 등록금 납부 8/25–8/29.",
            receivedAt = now.minusHours(11),
            isImportant = true,
            type = NotificationType.FINANCIAL,
            remindAt = now.plusDays(11).withHour(12).withMinute(0),
            isReminded = false,
            actions = listOf(
                NotificationAction(id = 116, title = "2학기 등록금 납부")
            )
        ),
        Notification(
            id = 17,
            appName = "헤이영캠퍼스",
            appPackageName = AppPackages.HEY_YOUNG,
            title = "지도교수 상담",
            content = "김민수 교수님: 다음 주 화요일 14시 연구실에서 진로 상담 가능합니다. 참석 여부를 회신해 주세요.",
            summary = "지도교수 상담 화요일 14시. 회신 필요.",
            receivedAt = yesterday(now).withHour(10).withMinute(15),
            isImportant = false,
            type = NotificationType.COMMUNICATION,
            remindAt = null,
            isReminded = false
        )
    ).map { it.copy(analysisStatus = AnalysisStatus.COMPLETED) }

    private fun yesterday(now: LocalDateTime): LocalDateTime = now.minusDays(1)
}
