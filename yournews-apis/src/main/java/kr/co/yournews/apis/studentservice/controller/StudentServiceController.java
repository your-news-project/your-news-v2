package kr.co.yournews.apis.studentservice.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.apis.studentservice.service.StudentServiceCommandService;
import kr.co.yournews.apis.studentservice.service.StudentServiceQueryService;
import kr.co.yournews.apis.studentservice.service.StudentServicePromotionService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-services")
@RequiredArgsConstructor
public class StudentServiceController {
    private final StudentServiceCommandService studentServiceCommandService;
    private final StudentServiceQueryService studentServiceQueryService;
    private final StudentServicePromotionService studentServicePromotionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid StudentServiceDto.Request request
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceCommandService.createStudentService(userDetails.getUserId(), request)
                )
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStudentServiceWithImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid StudentServiceDto.Request request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceCommandService.createStudentService(
                                userDetails.getUserId(),
                                request,
                                images
                        )
                )
        );
    }

    @PatchMapping(value = "/{studentServiceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId,
            @RequestBody @Valid StudentServiceDto.UpdateRequest request
    ) {
        studentServiceCommandService.updateStudentService(
                userDetails.getUserId(),
                studentServiceId,
                request,
                List.of()
        );
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping(value = "/{studentServiceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateStudentServiceWithImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId,
            @RequestPart("request") @Valid StudentServiceDto.UpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        studentServiceCommandService.updateStudentService(
                userDetails.getUserId(),
                studentServiceId,
                request,
                images
        );
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping
    public ResponseEntity<?> getStudentServices(
            @RequestParam(required = false) StudentServiceContentType contentType,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceQueryService.getStudentServices(contentType, pageable)
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyStudentServices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceQueryService.getMyStudentServices(userDetails.getUserId(), pageable)
                )
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularStudentServices() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceQueryService.getPopularStudentServices()
                )
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestStudentServices() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceQueryService.getLatestStudentServices()
                )
        );
    }

    @GetMapping("/promotions")
    public ResponseEntity<?> getPromotedStudentServices() {
        return ResponseEntity.ok(
                SuccessResponse.from(studentServicePromotionService.getActivePromotions())
        );
    }

    @PostMapping("/{studentServiceId}/promotions/reward")
    public ResponseEntity<?> rewardStudentServicePromotion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServicePromotionService.addReward(
                                userDetails.getUserId(),
                                studentServiceId
                        )
                )
        );
    }

    @GetMapping("/{studentServiceId}")
    public ResponseEntity<?> getStudentServiceById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;

        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceQueryService.getStudentServiceById(studentServiceId, userId)
                )
        );
    }

    @PostMapping("/{studentServiceId}/report")
    public ResponseEntity<?> reportStudentService(@PathVariable Long studentServiceId) {
        studentServiceCommandService.reportStudentService(studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/{studentServiceId}/click")
    public ResponseEntity<?> clickStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        studentServiceCommandService.clickStudentService(
                studentServiceId,
                userDetails.getUserId()
        );
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/{studentServiceId}/view")
    public ResponseEntity<?> viewStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        boolean increased = studentServiceCommandService.viewStudentService(
                studentServiceId,
                userDetails.getUserId()
        );
        return ResponseEntity.ok(SuccessResponse.from(increased));
    }

    @PostMapping("/{studentServiceId}/like")
    public ResponseEntity<?> likeStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        studentServiceCommandService.likeStudentService(userDetails.getUserId(), studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("/{studentServiceId}/like")
    public ResponseEntity<?> unlikeStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        studentServiceCommandService.unlikeStudentService(userDetails.getUserId(), studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("/{studentServiceId}")
    public ResponseEntity<?> deleteStudentService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studentServiceId
    ) {
        studentServiceCommandService.deleteStudentService(userDetails.getUserId(), studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

}
