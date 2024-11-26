package com.onion.backend.controller;

import com.onion.backend.dto.WriteDeviceDto;
import com.onion.backend.dto.WriteNoticeDto;
import com.onion.backend.entity.Device;
import com.onion.backend.entity.Notice;
import com.onion.backend.service.NoticeService;
import com.onion.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;
    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 공지사항 추가
     * @param dto
     * @return
     */
    @PostMapping("")
    public ResponseEntity<Notice> addNotice(@RequestBody WriteNoticeDto dto){
        return ResponseEntity.ok(noticeService.writeNotice(dto));
    }

    /**
     *  공지사항 조회
     * @param noticeId
     * @return
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<Notice> getNotice(@PathVariable(value = "noticeId") Long noticeId){
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

}
