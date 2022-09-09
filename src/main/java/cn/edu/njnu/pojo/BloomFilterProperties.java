package cn.edu.njnu.pojo;

import org.springframework.stereotype.Component;

@Component
public class BloomFilterProperties {
    /**
     * 预期插入量
     */
    private Long expectedInsertions = 50000L;
    /**
     * 误判率（大于0，小于1.0）
     */
    private Double fpp = 0.001D;

    public Long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(Long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public Double getFpp() {
        return fpp;
    }

    public void setFpp(Double fpp) {
        this.fpp = fpp;
    }
}
