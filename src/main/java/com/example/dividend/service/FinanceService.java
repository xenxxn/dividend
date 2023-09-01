package com.example.dividend.service;

import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.CompanyRepository;
import com.example.dividend.persist.DividendRepository;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity companyEntity =
                this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명 입니다."));
        //2. 조회된 회사의 id로 배당금 정보 조회
        List<DividendEntity> dividendEntityList =
                this.dividendRepository.findAllByCompanyId(companyEntity.getId());
        //3. 결과 조합 후 반환 (2가지 방법으로 만들 수 있다.)
//        List<Dividend> dividendList = new ArrayList<>();
//
//        for (var entity : dividendEntityList) {
//            dividendList.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }

        List<Dividend> dividendList = dividendEntityList.stream()
                .map(e -> Dividend.builder()
                        .date(e.getDate())
                        .dividend(e.getDividend())
                        .build())
                .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                                .ticker(companyEntity.getTicker())
                                .name(companyEntity.getName())
                                .build(), dividendList);
    }
}
