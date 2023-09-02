package com.example.dividend.scheduler;

import com.example.dividend.model.Company;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.CompanyRepository;
import com.example.dividend.persist.DividendRepository;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.dividend.model.constants.CacheKey.KEY_FINANCE;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

//    @Scheduled(fixedDelay = 1000)
//    public void test1() throws InterruptedException {
//        Thread.sleep(10000);
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 1: " + LocalDateTime.now());
//    }
//
//    @Scheduled(fixedDelay = 1000)
//    public void test2() {
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 2: " + LocalDateTime.now());
//    }


    // 일정 주기마다 수행
    @CacheEvict(value = KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("scraping scheduler is started");
        //저장된 회사 목록을 조회
        List<CompanyEntity> companyEntityList = this.companyRepository.findAll();
        //회사마다 배당금 정보를 새로 스크래핑
        for (var companyEntity : companyEntityList) {
            log.info("scraping scheduler is started : " + companyEntity.getName());
            ScrapedResult scrapedResult =
                    this.yahooFinanceScraper.scrap(
                            new Company(
                                    companyEntity.getTicker(),
                                    companyEntity.getName()));

            //스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividendEntities().stream()
                    //Dividend 모델을 Dividend Entity 로 매핑
                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                    //존재하지 않는 경우 element 를 하나씩 dividend repository 에 삽입
                    .forEach(e -> {
                        boolean exists =
                                this.dividendRepository.existsByCompanyIdAndDate(
                                        e.getCompanyId(),
                                        e.getDate()
                                );
                        if (!exists) {
                            this.dividendRepository.save(e);
                        }
                    });
            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시 정지
            try {
                Thread.sleep(3000); //3초
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
