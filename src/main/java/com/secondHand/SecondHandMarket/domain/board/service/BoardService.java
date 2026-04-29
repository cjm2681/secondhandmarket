package com.secondHand.SecondHandMarket.domain.board.service;

import com.secondHand.SecondHandMarket.domain.board.dto.BoardCreateRequest;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardListResponse;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardResponse;
import com.secondHand.SecondHandMarket.domain.board.dto.BoardUpdateRequest;
import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import com.secondHand.SecondHandMarket.domain.board.repository.BoardRepository;
import com.secondHand.SecondHandMarket.domain.board.repository.CommentRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private final RedisTemplate<String, String> redisTemplate;

    // 게시글 작성
    @Transactional
    public BoardResponse create(Long userId, BoardCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = Board.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Board saved = boardRepository.save(board);
        return BoardResponse.from(saved, List.of());
    }

    // 게시글 목록 조회
    public Page<BoardListResponse> getList(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return boardRepository
                .findAllByKeyword(keyword, pageable)
                .map(BoardListResponse::from);
    }

    // 게시글 상세 조회 + 조회수 증가
    @Transactional
    public BoardResponse getDetail(Long boardId,
                                   HttpServletRequest request) {
        Board board = boardRepository.findByIdWithUser(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));


        String viewer = getViewerIdentifier(request);

        // Redis TTL 기반 중복 조회 방지
        // 키 구조: "VIEW:BOARD:{boardId}:{userId or IP}"
        // TTL = 1시간 → 1시간 후 키 자동 만료 → 동일 사용자도 재조회 가능
        // 서버 메모리(세션) 방식 대신 Redis 사용 이유:
        //   세션은 서버 메모리에 저장 → 서버 여러 대 환경에서 공유 불가
        //   Redis는 서버 바깥의 공용 저장소 → 다중 서버 환경에서도 일관 동작
        String key = "VIEW:BOARD:" + boardId + ":" + viewer;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            board.increaseViewCount(); // Dirty Checking → 트랜잭션 커밋 시 자동 UPDATE
            redisTemplate.opsForValue().set(key, "1", 1L, TimeUnit.HOURS);
        }
        List<Comment> comments = commentRepository.findByBoardIdWithChildren(boardId);
        return BoardResponse.from(board, comments);
    }


    // 조회자 식별자 추출
    // 로그인: "USER:{userId}" → userId 기반으로 정확한 식별
    // 비로그인: "IP:{ip}" → IP 기반으로 어뷰징 방지
    private String getViewerIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long userId) {
            return "USER:" + userId;
        }

        // X-Forwarded-For: 프록시/로드밸런서 환경에서 실제 클라이언트 IP 추출
        // 없으면 직접 연결된 IP 사용
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return "IP:" + ip;
    }

    // 게시글 수정
    @Transactional
    public BoardResponse update(Long boardId, Long userId, BoardUpdateRequest request) {
        Board board = getBoardOrThrow(boardId);
        checkOwnership(board, userId);

        board.update(request.getTitle(), request.getContent());

        List<Comment> comments = commentRepository.findByBoardIdWithChildren(boardId);
        return BoardResponse.from(board, comments);
    }

    // 게시글 삭제 (소프트 삭제)
    @Transactional
    public void delete(Long boardId, Long userId, String role) {
        Board board = getBoardOrThrow(boardId);

        if (!board.getUser().getId().equals(userId) && !role.equals("ADMIN")) {
            throw new CustomException(ErrorCode.BOARD_FORBIDDEN);
        }

        board.delete();
    }

    private Board getBoardOrThrow(Long boardId) {
        return boardRepository.findById(boardId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private void checkOwnership(Board board, Long userId) {
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOARD_FORBIDDEN);
        }
    }


    public Page<BoardListResponse> getMyBoards(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return boardRepository
                .findByUserIdAndIsDeletedFalse(userId, pageable)
                .map(BoardListResponse::from);
    }


}