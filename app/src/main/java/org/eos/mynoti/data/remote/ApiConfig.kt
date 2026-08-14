package org.eos.mynoti.data.remote

import org.eos.mynoti.BuildConfig

/**
 * 백엔드 접속 정보는 여기와 BuildConfig에서만 관리한다.
 * README: uvicorn --port 8000, 인증 헤더 X-API-Key.
 * 에뮬레이터는 localhost 대신 10.0.2.2를 사용한다.
 */
object ApiConfig {
    val baseUrl: String = BuildConfig.API_BASE_URL
    val apiKey: String = BuildConfig.API_KEY
    const val API_KEY_HEADER = "X-API-Key"
}
