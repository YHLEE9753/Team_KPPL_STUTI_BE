package prgrms.project.stuti.domain.studygroup.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static prgrms.project.stuti.domain.studygroup.controller.CommonStudyGroupTestUtils.CommonField.*;
import static prgrms.project.stuti.domain.studygroup.controller.CommonStudyGroupTestUtils.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import prgrms.project.stuti.config.TestConfig;
import prgrms.project.stuti.domain.member.model.Career;
import prgrms.project.stuti.domain.member.model.Mbti;
import prgrms.project.stuti.domain.studygroup.model.Region;
import prgrms.project.stuti.domain.studygroup.model.Topic;
import prgrms.project.stuti.domain.studygroup.service.response.LeaderResponse;
import prgrms.project.stuti.domain.studygroup.service.response.StudyGroupDetailResponse;
import prgrms.project.stuti.domain.studygroup.service.response.StudyGroupIdResponse;
import prgrms.project.stuti.domain.studygroup.service.studygroup.StudyGroupService;

@WebMvcTest(controllers = StudyGroupRestController.class)
class StudyGroupRestControllerTest extends TestConfig {

	@MockBean
	private StudyGroupService studyGroupService;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Test
	@DisplayName("새로운 스터디 그룹을 생성한다.")
	void postStudyGroup() throws Exception {
		//given
		StudyGroupIdResponse idResponse = new StudyGroupIdResponse(1L);
		MultiValueMap<String, String> createParams = toCreateParams();
		given(studyGroupService.createStudyGroup(any())).willReturn(idResponse);

		//when
		ResultActions resultActions = mockMvc.perform(multipart("/api/v1/study-groups")
			.file("imageFile", getMultipartFileBytes())
			.params(createParams)
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.with(requestPostProcessor -> {
				requestPostProcessor.setMethod("POST");
				return requestPostProcessor;
			}));

		//then
		resultActions
			.andExpectAll(
				status().isCreated(),
				content().json(objectMapper.writeValueAsString(idResponse)))
			.andDo(
				document(COMMON_DOCS_NAME,
					requestHeaders(contentType(), host()),
					requestParts(imageFile()),
					requestParameters(title()).and(parametersOfCreateStudyGroup()).and(description()),
					responseHeaders(contentType()).and(location()),
					responseFields(studyGroupIdField())));
	}

	@Test
	@DisplayName("스터디 그룹을 상세조회한다.")
	void getStudyGroup() throws Exception {
		//given
		StudyGroupDetailResponse detailResponse = toDetailResponse();
		given(studyGroupService.getStudyGroup(any())).willReturn(detailResponse);

		//when
		ResultActions resultActions = mockMvc.perform(
			get("/api/v1/study-groups/{studyGroupId}",
				detailResponse.studyGroupId()).contentType(APPLICATION_JSON));

		//then
		resultActions
			.andExpectAll(
				status().isOk(),
				content().json(objectMapper.writeValueAsString(detailResponse)))
			.andDo(
				document(COMMON_DOCS_NAME,
					requestHeaders(contentType(), host()),
					pathParameters(studyGroupIdPath()),
					responseHeaders(contentType()),
					responseFields(toStudyGroupDetail()).andWithPrefix("leader.", toLeaderFields())
				));
	}

	@Test
	@DisplayName("스터디 그룹을 업데이트한다.")
	void patchStudyGroup() throws Exception {
		//given
		StudyGroupIdResponse idResponse = new StudyGroupIdResponse(1L);
		MultiValueMap<String, String> updateParams = toUpdateParams();
		given(studyGroupService.updateStudyGroup(any())).willReturn(idResponse);

		//when
		ResultActions resultActions = mockMvc.perform(
			multipart("/api/v1/study-groups/{studyGroupId}", idResponse.studyGroupId())
				.file("imageFile", getMultipartFileBytes())
				.params(updateParams)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.with(requestPostProcessor -> {
					requestPostProcessor.setMethod("PATCH");
					return requestPostProcessor;
				}));

		//then
		resultActions
			.andExpectAll(
				status().isOk(),
				content().json(objectMapper.writeValueAsString(idResponse)))
			.andDo(
				document(COMMON_DOCS_NAME,
					requestHeaders(contentType(), host()),
					requestParts(imageFile()),
					requestParameters(title(), description()),
					responseHeaders(contentType()),
					responseFields(studyGroupIdField())));
	}

	@Test
	@DisplayName("스터디 그룹을 삭제한다.")
	void deleteStudyGroup() throws Exception {
		//given
		doNothing().when(studyGroupService).deleteStudyGroup(any(), any());

		//when
		ResultActions resultActions = mockMvc.perform(
			delete("/api/v1/study-groups/{studyGroupId}", 1L)
				.contentType(APPLICATION_JSON));

		// then
		resultActions
			.andExpectAll(
				status().isOk(),
				content().contentType(APPLICATION_JSON))
			.andDo(
				document(COMMON_DOCS_NAME,
					requestHeaders(contentType(), host()),
					pathParameters(studyGroupIdPath()),
					responseHeaders(contentType())));
	}

	private MultiValueMap<String, String> toCreateParams() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(TITLE.value(), "test title");
		map.add(TOPIC.value(), String.valueOf(Topic.AI));
		map.add(IS_ONLINE.value(), "false");
		map.add(REGION.value(), String.valueOf(Region.SEOUL));
		map.add(PREFERRED_MBTIS.value(), "INFJ, ENFP");
		map.add(NUMBER_OF_RECRUITS.value(), "5");
		map.add(START_DATE_TIME.value(), LocalDateTime.now().plusDays(10).format(dateTimeFormatter));
		map.add(END_DATE_TIME.value(), LocalDateTime.now().plusMonths(10).format(dateTimeFormatter));
		map.add(DESCRIPTION.value(), "test description");

		return map;
	}

	private StudyGroupDetailResponse toDetailResponse() {

		return StudyGroupDetailResponse
			.builder()
			.studyGroupId(1L)
			.topic(Topic.DEV_OPS.getValue())
			.title("test title")
			.imageUrl("test image url")
			.leader(
				LeaderResponse.builder()
					.memberId(1L)
					.profileImageUrl("test profile image url")
					.nickname("nickname")
					.field("BACKEND")
					.career(
						Career.JUNIOR.getCareerValue())
					.mbti(Mbti.ENFJ.name())
					.build()
			)
			.preferredMBTIs(List.of("ENFJ", "INFJ"))
			.isOnline(false)
			.region(Region.DAEJEON.getValue())
			.startDateTime(LocalDateTime.now().plusDays(10))
			.endDateTime(LocalDateTime.now().plusMonths(10))
			.numberOfMembers(5)
			.numberOfRecruits(5)
			.description("test description")
			.build();
	}

	private MultiValueMap<String, String> toUpdateParams() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(TITLE.value(), "update title");
		map.add(DESCRIPTION.value(), "update description");

		return map;
	}

	private byte[] getMultipartFileBytes() throws IOException {
		return new MockMultipartFile("testImageFile", "testImageFile.png",
			"image/png", "test".getBytes()).getBytes();
	}

	private HeaderDescriptor location() {
		return headerWithName(HttpHeaders.LOCATION).description("생성된 리소스 주소");
	}

	private ParameterDescriptor title() {
		return parameterWithName(TITLE.value()).description("스터디 그룹 제목");
	}

	private ParameterDescriptor description() {
		return parameterWithName(DESCRIPTION.value()).description("스터디 그룹 설명");
	}

	private List<ParameterDescriptor> parametersOfCreateStudyGroup() {
		return List.of(parameterWithName(TOPIC.value()).description("스터디 주제"),
			parameterWithName(IS_ONLINE.value()).description("온라인 / 오프라인 여부"),
			parameterWithName(REGION.value()).description("지역"),
			parameterWithName(PREFERRED_MBTIS.value()).description("선호하는 MBTI 목록"),
			parameterWithName(NUMBER_OF_RECRUITS.value()).description("모집 인원수"),
			parameterWithName(START_DATE_TIME.value()).description("스터디 시작일자"),
			parameterWithName(END_DATE_TIME.value()).description("스터디 종료일자"));
	}

	private RequestPartDescriptor imageFile() {
		return partWithName(IMAGE_FILE.value()).description("이미지 파일");
	}

	private FieldDescriptor studyGroupIdField() {
		return fieldWithPath(STUDY_GROUP_ID.value()).type(NUMBER).description("스터디 그룹 아이디");
	}

	private List<FieldDescriptor> toStudyGroupDetail() {
		return List.of(
			studyGroupIdField(),
			fieldWithPath(TOPIC.value()).type(STRING).description("스터디 주제"),
			fieldWithPath(TITLE.value()).type(STRING).description("스터디 그룹 제목"),
			fieldWithPath(IMAGE_URL.value()).type(STRING).description("스터디 이미지 url"),
			fieldWithPath(PREFERRED_MBTIS.value()).type(ARRAY).description("선호하는 MBTI 목록"),
			fieldWithPath(IS_ONLINE.value()).type(BOOLEAN).description("온라인 / 오프라인 여부"),
			fieldWithPath(REGION.value()).type(STRING).description("지역"),
			fieldWithPath(START_DATE_TIME.value()).type(STRING).description("스터디 시작일자"),
			fieldWithPath(END_DATE_TIME.value()).type(STRING).description("스터디 종료일자"),
			fieldWithPath(NUMBER_OF_MEMBERS.value()).type(NUMBER).description("스터디 멤버 인원수"),
			fieldWithPath(NUMBER_OF_RECRUITS.value()).type(NUMBER).description("모집 인원수"),
			fieldWithPath(DESCRIPTION.value()).type(STRING).description("스터디 그룹 설명")
		);
	}

	private List<FieldDescriptor> toLeaderFields() {
		return List.of(
			fieldWithPath(MEMBER_ID.value()).type(NUMBER).description("회원 아이디"),
			fieldWithPath(PROFILE_IMAGE_URL.value()).type(STRING).description("프로필 이미지 url"),
			fieldWithPath(NICKNAME.value()).type(STRING).description("닉네임"),
			fieldWithPath(FIELD.value()).type(STRING).description("업무분야"),
			fieldWithPath(CAREER.value()).type(STRING).description("개발경력"),
			fieldWithPath(MBTI.value()).type(STRING).description("MBTI")
		);
	}
}

