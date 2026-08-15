"""패키지 exact / 키워드 우선순위로 앱별 프롬프트가 갈리는지 확인합니다."""

from app.prompts import (
    DEFAULT_ADDON,
    FINANCIAL_ADDON,
    HEYYOUNG_ADDON,
    INSTAGRAM_ADDON,
    KAKAOTALK_ADDON,
    LEARNINGX_ADDON,
    get_system_prompt,
)


def _addon_of(package_name: str, app_name: str = "") -> str:
    prompt = get_system_prompt(package_name, app_name)
    for addon in (
        LEARNINGX_ADDON,
        HEYYOUNG_ADDON,
        KAKAOTALK_ADDON,
        INSTAGRAM_ADDON,
        FINANCIAL_ADDON,
        DEFAULT_ADDON,
    ):
        if prompt.endswith(addon):
            return addon
    raise AssertionError(f"unknown addon for {package_name!r} {app_name!r}")


def test_kakaobank_uses_financial_addon_not_kakaotalk():
    assert _addon_of("com.kakaobank.channel", "카카오뱅크") == FINANCIAL_ADDON
    assert KAKAOTALK_ADDON not in get_system_prompt("com.kakaobank.channel", "카카오뱅크")


def test_kakaotalk_uses_messenger_addon():
    assert _addon_of("com.kakao.talk", "KakaoTalk") == KAKAOTALK_ADDON
    assert FINANCIAL_ADDON not in get_system_prompt("com.kakao.talk", "KakaoTalk")


def test_default_five_apps_exact_packages():
    assert _addon_of("com.instructure.candroid.xinics2.production") == LEARNINGX_ADDON
    assert _addon_of("com.shinhan.heyoung") == HEYYOUNG_ADDON
    assert _addon_of("com.kakao.talk") == KAKAOTALK_ADDON
    assert _addon_of("com.shcard.smartpay") == FINANCIAL_ADDON
    assert _addon_of("com.kakaobank.channel") == FINANCIAL_ADDON


def test_keyword_fallback_kakaobank_before_kakao():
    assert _addon_of("com.example.kakaobank.unofficial") == FINANCIAL_ADDON
    assert _addon_of("com.example.kakaopay") == FINANCIAL_ADDON
    assert _addon_of("com.example.kakao.lite") == KAKAOTALK_ADDON


def test_unknown_package_uses_default_addon():
    assert _addon_of("com.android.chrome", "Chrome") == DEFAULT_ADDON
