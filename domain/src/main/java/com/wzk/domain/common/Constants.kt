package com.wzk.domain.common

/**
 * The project's properties
 */
object Constants {
    /**
     * Edit this value to change your server url
     *
     * Pay attention: this value should start with "http://" and end with "/",
     * backend server's port is 8080 as default
     * ---
     * If you run the backend in your computer, you need get its IPv4 address
     *
     * In windows, open CMD and type:
     * ```shell
     * ipconfig /all
     * ```
     * The IPv4 Address is the value you needed
     */
    const val BASE_URL = "http://192.168.5.7:8080/"

    /**
     * Please edit this value.
     *
     * This value determines whether this project will use the real server as the data source
     * or fake data for demonstration purposes.
     *
     * True - this project will use fake data and you needn't configure your backend
     *
     * False - config BASE_URL first then it will use the real server
     */
    const val MOCK_MODE = true

    /**
     * DON'T CHANGE THESE VALUE
     */
    const val DB_NAME = "db"
    const val PARAM_FOOD_ID = "foodId"
}