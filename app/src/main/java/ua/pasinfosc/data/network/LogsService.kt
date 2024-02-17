package ua.pasinfosc.data.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface LogsService {

//    @Headers(
//        ":Authority: hatebin.com",
//        ":Method: POST",
//        ":Path: /index.php",
//        ":Scheme: https"
//    )
    @POST
    suspend fun log(
        @Url url: String,
        @Body body: String = "",
//        @Query("api_dev_key") devKey: String = "0411723f73c3e2c410c452c791d2f447",
//        @Query("api_paste_private") private: Int = 1,
//        @Query("api_paste_name", encoded = true) name: String = "logs (${Calendar.getInstance().let {
//            "${it.get(Calendar.HOUR_OF_DAY)}:${it.get(Calendar.MINUTE)} ${it.get(Calendar.DAY_OF_MONTH)}.${it.get(Calendar.MONTH) + 1}"
//        }})",
//        @Query("api_paste_expire_date") expireDate: String = "N",
//        @Query("api_option") apiOption: String = "paste",
    ): retrofit2.Response<String>
}