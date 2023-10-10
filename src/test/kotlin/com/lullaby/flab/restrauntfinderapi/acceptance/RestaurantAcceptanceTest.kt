package com.lullaby.flab.restrauntfinderapi.acceptance

import com.lullaby.flab.restrauntfinderapi.acceptance.fixture.로그인
import com.lullaby.flab.restrauntfinderapi.acceptance.fixture.식당_생성
import com.lullaby.flab.restrauntfinderapi.acceptance.fixture.식당_조회
import com.lullaby.flab.restrauntfinderapi.acceptance.fixture.회원_가입
import com.lullaby.flab.restrauntfinderapi.application.restaurant.command.CreateRestaurantCommand
import com.lullaby.flab.restrauntfinderapi.application.restaurant.response.RestaurantResponse
import com.lullaby.flab.restrauntfinderapi.domain.FoodType
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class RestaurantAcceptanceTest : AcceptanceTest() {

    var accessToken: String? = null

    @BeforeEach
    fun beforeEach() {
        회원_가입("user", "1234")
        accessToken = 로그인("user", "1234")
    }

    @DisplayName("식당을 생성 한다.")
    @Test
    fun create() {
        val restaurantResponse = 식당_생성(accessToken!!, "강남교자 본점", "서울 서초구 강남대로69길 11 삼미빌딩", 20, FoodType.KOREAN)
        assertThat(restaurantResponse.id).isNotNull
    }

    @DisplayName("식당을 생성한 후 조회 하면 식당이 조회 된다.")
    @Test
    fun lists() {
        식당_생성(accessToken!!, "강남교자 본점", "서울 서초구 강남대로69길 11 삼미빌딩", 20, FoodType.KOREAN)
        식당_생성(accessToken!!, "쉐이크쉑 강남대로점", "서울 서초구 강남대로 421", 30, FoodType.WESTERN)

        val restaurantResponses = 식당_조회(accessToken!!)
        assertThat(restaurantResponses.size).isEqualTo(2)
    }

    @DisplayName("식당에 메뉴를 추가 한다.")
    @Test
    fun createMenu() {
        val restaurantResponse = 식당_생성(
            accessToken!!,
            "강남교자 본점",
            "서울 서초구 강남대로69길 11 삼미빌딩",
            20,
            FoodType.KOREAN
        )

        val command = mapOf(
            "name" to "교자 칼국수",
            "price" to "11000",
            "type" to "MAIN"
        )

        val response = RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .body(command)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .`when`().post("/restaurants/${restaurantResponse.id}/menus")
            .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
    }

}
