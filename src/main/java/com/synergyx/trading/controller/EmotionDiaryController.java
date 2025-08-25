package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryRequestDTO;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import com.synergyx.trading.service.emotionDiaryService.EmotionDiaryCommandService;
import com.synergyx.trading.service.emotionDiaryService.EmotionDiaryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
@Tag(name = "감정 투자 일기 API", description = "감정 투자 일기 관련 API 입니다.")
public class EmotionDiaryController {

    // 임시 userId
    private static final Long TEMP_USER_ID = 1L;

    private final EmotionDiaryCommandService emotionDiaryCommandService;
    private final EmotionDiaryQueryService emotionDiaryQueryService;

    // 감정 투자 일기 작성
    @Operation(summary = "감정 투자 일기 작성", description = "감정 투자 일기를 작성합니다.")
    @PostMapping
    public ResponseEntity<?> writeDiary(
           @Valid @RequestBody EmotionDiaryRequestDTO request) {
        EmotionDiaryResponseDTO.EmotionDiaryDTO result = emotionDiaryCommandService.writeDiary(TEMP_USER_ID, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                result,
                SuccessStatus.SUCCESS_WRITE_EMOTION_DIARY.getCode(),
                SuccessStatus.SUCCESS_WRITE_EMOTION_DIARY.getMessage()
        ));
    }

    // 감정 투자 일기 전체 조회
    @Operation(summary = "감정 투자 일기 목록 조회", description = "감정 투자 일기 전체를 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getEmotionDiaryList() {
        List<EmotionDiaryResponseDTO.EmotionDiaryDTO> result = emotionDiaryQueryService.getEmotionDiaryList(TEMP_USER_ID);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    // 감정 투자 일기 삭제
    @Operation(summary = "감정 투자 일기 삭제", description = "감정 투자 일기를 삭제합니다.")
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(
            @Parameter
            @PathVariable Long diaryId) {
        emotionDiaryCommandService.deleteDiary(TEMP_USER_ID, diaryId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_DELETE_EMOTION_DIARY.getCode(),
                SuccessStatus.SUCCESS_DELETE_EMOTION_DIARY.getMessage()
        ));
    }

}
