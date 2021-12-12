package nextstep.subway.path.acceptance;

import static nextstep.subway.line.acceptance.step.LineAcceptanceStep.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.step.LineSectionAcceptanceStep.지하철_노선에_지하철역_등록_요청;
import static nextstep.subway.member.step.FavoriteAuthAcceptanceFixtures.로그인_토큰_발급;
import static nextstep.subway.station.step.StationAcceptanceStep.지하철역_등록되어_있음;
import static nextstep.subway.path.step.PathAcceptanceStep.*;
import static nextstep.subway.path.acceptance.PathAcceptanceFixtures.*;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.line.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("지하철 경로 조회 관련 기능")
class PathAcceptanceTest extends AcceptanceTest {

    private String 어린이유저;
    private String 청소년유저;
    private String 성인유저;

    @BeforeEach
    public void setUp() {
        super.setUp();
        어린이유저 = 로그인_토큰_발급("child@email.com", "11", 10);
        청소년유저 = 로그인_토큰_발급("youth@email.com", "11", 15);
        성인유저 = 로그인_토큰_발급("man@email.com", "11", 30);

        강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = 지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = 지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);
        천호역 = 지하철역_등록되어_있음("천호").as(StationResponse.class);
        강동구청 = 지하철역_등록되어_있음("강동구청").as(StationResponse.class);
        없는역 = 지하철역_등록되어_있음("없는역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10, 0).as(
            LineResponse.class);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역.getId(), 강남역.getId(), 10, 900).as(
            LineResponse.class);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 5, 500).as(
            LineResponse.class);
        팔호선 = 지하철_노선_등록되어_있음("팔호선", "bg-red-600", 천호역.getId(), 강동구청.getId(), 5, 0).as(
            LineResponse.class);

        지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
    }


    @Test
    void 최단경로_조회() {
        // when
        ExtractableResponse<Response> response = 경로_조회(강남역.getId(), 남부터미널역.getId());

        // then
        최단경로_조회_됨(response, Arrays.asList(강남역.getId(), 양재역.getId(), 남부터미널역.getId()));
        최단경로_조회_길이_계산됨(response, 12);
        최단경로_요금_계산됨(response, 1350);
    }

    @Test
    void 최단경로_조회_연령별_요금_조회() {
        // when
        ExtractableResponse<Response> 어린이경로_응답 = 로그인_경로_조회(강남역.getId(), 남부터미널역.getId(), 어린이유저);
        // then
        최단경로_조회_됨(어린이경로_응답, Arrays.asList(강남역.getId(), 양재역.getId(), 남부터미널역.getId()));
        최단경로_조회_길이_계산됨(어린이경로_응답, 12);
        최단경로_요금_계산됨(어린이경로_응답, 500);

        // when
        ExtractableResponse<Response> 청소년경로_응답 = 로그인_경로_조회(강남역.getId(), 남부터미널역.getId(), 청소년유저);
        // then
        최단경로_조회_됨(청소년경로_응답, Arrays.asList(강남역.getId(), 양재역.getId(), 남부터미널역.getId()));
        최단경로_요금_계산됨(청소년경로_응답, 800);

        // when
        ExtractableResponse<Response> 성인경로_응답 = 로그인_경로_조회(강남역.getId(), 남부터미널역.getId(), 성인유저);
        // then
        최단경로_조회_됨(성인경로_응답, Arrays.asList(강남역.getId(), 양재역.getId(), 남부터미널역.getId()));
        최단경로_요금_계산됨(성인경로_응답, 1350);
    }


    @Test
    void 같은_역_경로_조회_실패() {
        // when
        ExtractableResponse<Response> response = 경로_조회(강남역.getId(), 강남역.getId());

        // then
        경로_조회_실패됨(response);
    }

    @Test
    void 이어지지_않는_경로_조회_실패() {
        // when
        ExtractableResponse<Response> response = 경로_조회(강남역.getId(), 천호역.getId());

        // then
        경로_조회_실패됨(response);
    }

    @Test
    void 구간에_역이_존재_하지않는_경우_실패() {
        // when
        ExtractableResponse<Response> response = 경로_조회(강남역.getId(), 없는역.getId());

        // then
        경로_조회_실패됨(response);
    }
}
