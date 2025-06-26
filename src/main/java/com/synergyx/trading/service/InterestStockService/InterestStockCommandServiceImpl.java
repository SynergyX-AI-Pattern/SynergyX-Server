package com.synergyx.trading.service.InterestStockService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.model.InterestStock;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.User;
import com.synergyx.trading.model.RecentViewStock;
import com.synergyx.trading.repository.InterestStockRepository;
import com.synergyx.trading.repository.RecentViewStockRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class InterestStockCommandServiceImpl implements InterestStockCommandService {

    private final InterestStockRepository interestStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final RecentViewStockRepository recentViewStockRepository;


    /**
     * 관심 종목을 등록합니다.
     *
     * @param userId
     * @param stockId
     */
    @Override
    @Transactional
    public void addInterest(Long userId, Long stockId) {
        if (interestStockRepository.findByUserIdAndStockId(userId, stockId).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 관심 종목입니다.");
        }
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 종목 조회
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        InterestStock entity = InterestStock.builder()
                .user(user)
                .stock(stock)
                .build();
        interestStockRepository.save(entity);
    }

    /**
     * 관심 종목을 삭제합니다.
     *
     * @param userId
     * @param stockId
     */
    @Override
    @Transactional
    public void removeInterest(Long userId, Long stockId) {
        InterestStock interestStock = interestStockRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.INTEREST_NOT_FOUND));

        interestStockRepository.delete(interestStock);
    }

    /**
     * 최근 조회 종목을 추가합니다.
     *
     * @param userId
     * @param stockId
     */
    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public void addRecentView(Long userId, Long stockId) {
        // 중복 제거
        recentViewStockRepository.deleteByUserIdAndStockId(userId, stockId);

        recentViewStockRepository.save(
                RecentViewStock.builder()
                        .userId(userId)
                        .stockId(stockId)
                        .viewedAt(LocalDateTime.now())
                        .build()
        );

        // 20개 초과 시 오래된 데이터 삭제
        recentViewStockRepository.deleteOverLimit(userId);
    }
}