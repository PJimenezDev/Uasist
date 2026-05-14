package com.dev.uasist.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer


object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://bcmlcfzmapimdtwoohjw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJjbWxjZnptYXBpbWR0d29vaGp3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc2MTU3NDcsImV4cCI6MjA5MzE5MTc0N30.Fl_3BBQEieLRtQ5PMhHtIF02_1mzv2v1iPo5jWQl0YQ"
    ) {
        httpEngine = OkHttp.create()
        install(Postgrest)
        install(Auth)
        install(Realtime)
        
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            encodeDefaults = true
        })
    }
}
