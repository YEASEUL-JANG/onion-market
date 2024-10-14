package com.onion.backend.controller;

import com.onion.backend.dto.CommentReqDto;
import com.onion.backend.dto.CommentResDto;
import com.onion.backend.entity.Comment;
import com.onion.backend.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class CommentController {
    private final CommentService commentService;
    private final AuthenticationManager authenticationManager;

    public CommentController( CommentService commentService, AuthenticationManager authenticationManager) {
        this.commentService = commentService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 댓글 발행
     * @param boardId
     * @return
     */
    @PostMapping("/{boardId}/articles/{articleId}/comment")
    public ResponseEntity<CommentResDto> writeComment(@PathVariable(value = "boardId") Long boardId,
                                                      @PathVariable(value = "articleId") Long articleId,
                                                      @RequestBody CommentReqDto commentReqDto) {
        return ResponseEntity.ok(commentService.writeComment(commentReqDto,boardId, articleId));
    }


    /**
     * 댓글 수정
     * @param boardId
     * @param articleId
     * @return
     */
    @PutMapping("/{boardId}/articles/{articleId}/comment/{commentId}")
    public ResponseEntity<CommentResDto> editComment(@PathVariable(value = "boardId") Long boardId,
                                                @PathVariable(value = "articleId") Long articleId,
                                                @PathVariable(value = "commentId") Long commentId,
                                                @RequestBody CommentReqDto commentReqDto) {
        return ResponseEntity.ok(commentService.editComment(articleId,commentId, commentReqDto));
    }

    /**
     * 댓글 삭제
     * @param boardId
     * @param articleId
     * @return
     */
    @DeleteMapping("/{boardId}/articles/{articleId}/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable(value = "boardId") Long boardId,
                                               @PathVariable(value = "articleId") Long articleId,
                                                @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteComment(articleId,commentId);
        return ResponseEntity.ok("success");
    }
}
