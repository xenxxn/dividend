package com.example.dividend.service;

import com.example.dividend.exception.impl.NoCompanyException;
import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.CompanyRepository;
import com.example.dividend.persist.DividendRepository;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.dividend.model.constants.CacheKey.KEY_FINANCE;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    //캐시를 사용할 떄 고려해야 할 점
    //요청이 자주 들어오는가?
    //자주 변경되는 데이터인가?

    @Cacheable(key = "#companyName", value = KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search companyName : " + companyName);
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity companyEntity =
                this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());
        //2. 조회된 회사의 id로 배당금 정보 조회
        List<DividendEntity> dividendEntityList =
                this.dividendRepository.findAllByCompanyId(companyEntity.getId());
        //3. 결과 조합 후 반환 (2가지 방법으로 만들 수 있다. 반복문, stream 중 사용)
//        List<Dividend> dividendList = new ArrayList<>();
//
//        for (var entity : dividendEntityList) {
//            dividendList.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }

        List<Dividend> dividendList = dividendEntityList.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(
                new Company(
                companyEntity.getTicker(),
                companyEntity.getName()),
                dividendList);
    }
}
