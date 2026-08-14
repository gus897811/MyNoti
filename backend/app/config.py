from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """환경변수 기반 설정. .env 파일에서 값을 읽어옵니다."""

    # 안드로이드 앱 <-> 백엔드 인증용 (해커톤이므로 고정 키 방식).
    # 대화에서 명시적으로 요구된 항목은 아니지만, OpenAI 키를 지키는 백엔드를
    # 아무나 호출하지 못하도록 최소한의 보호 장치로 추가했습니다. 필요 없으면
    # app/auth.py의 dependencies만 빼면 됩니다.
    API_KEY: str = "dev-api-key-change-me"

    # 백엔드 <-> OpenAI API. 이 키는 절대 안드로이드 앱에 넣지 않습니다.
    OPENAI_API_KEY: str = ""
    # 모델명은 자주 바뀌므로 배포 전 https://platform.openai.com/docs/models 에서 확인하세요.
    LLM_MODEL: str = "gpt-5.6-terra"
    LLM_TIMEOUT_SEC: float = 5.0

    class Config:
        env_file = ".env"


settings = Settings()
