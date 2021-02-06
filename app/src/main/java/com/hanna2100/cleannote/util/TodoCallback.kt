package com.hanna2100.cleannote.util

// 어떤 메소드가 호출된 이후, 뭔갈 진행해야할 때 쓰는 간단한 콜백 인터페이스
interface TodoCallback {
    fun execute()
}