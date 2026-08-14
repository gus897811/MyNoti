class APIError(Exception):
    """{ "error": { "code", "message" } } 포맷으로 변환되는 예외."""

    def __init__(self, status_code: int, code: str, message: str):
        self.status_code = status_code
        self.code = code
        self.message = message
        super().__init__(message)
