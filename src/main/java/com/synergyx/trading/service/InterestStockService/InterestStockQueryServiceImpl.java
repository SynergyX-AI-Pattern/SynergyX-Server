package com.synergyx.trading.service.InterestStockService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.InterestStock.InterestStockResponseDTO;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.InterestStockRepository;
import com.synergyx.trading.repository.RecentViewStockRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestStockQueryServiceImpl implements InterestStockQueryService {

    private final InterestStockRepository interestStockRepository;
    private final UserRepository userRepository;
    private final RecentViewStockRepository recentViewStockRepository;

    /**
     * 관심 종목 목록을 조회합니다.
     *
     * @param userId
     * @return interestStockResponseDTO list
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterestStockResponseDTO> getInterestList(Long userId) {

        User user = getValidUser(userId);

        return interestStockRepository.findAllByUserId(userId).stream()
                .map(i -> InterestStockResponseDTO.builder()
                        .stockId(i.getStock().getId())
                        .stockName(i.getStock().getName())
                        .stockSymbol(i.getStock().getSymbol())
                        .imageUrl(i.getStock().getImageUrl())
                        .build()
                ).toList();
    }

    /**
     * 최근 조회 종목 리스트를 조회합니다.
     *
     * @param userId
     * @return InterestStockResponseDTO list
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterestStockResponseDTO> getRecentViewStocks(Long userId) {
        User user = getValidUser(userId);

        return recentViewStockRepository.findRecentStocksWithInfo(userId);
    }

    /**
     * 유저 존재 여부를 확인합니다.
     */
    private User getValidUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }
}
