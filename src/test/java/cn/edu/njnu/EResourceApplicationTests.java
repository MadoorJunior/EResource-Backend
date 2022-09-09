package cn.edu.njnu;


import cn.edu.njnu.filter.RedisBloomFilter;
import cn.edu.njnu.mapper.RoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class EResourceApplicationTests {

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Autowired
    private RoleMapper roleMapper;
    @Test
    public void testMapper(){
        String roleName = roleMapper.getRoleNameByUserName("麦的垛");
        System.out.println(roleName);
    }
    @Test
    public void testBloomFilter(){
        System.out.println(redisBloomFilter.mightContain("知识点", "的"));
        System.out.println(redisBloomFilter.mightContain("知识点", "哈哈"));
        System.out.println(redisBloomFilter.mightContain("知识点", "测试字符串"));
        System.out.println(redisBloomFilter.mightContain("知识点", "化学"));
        System.out.println(redisBloomFilter.mightContain("知识点", "什么东西"));
        System.out.println(redisBloomFilter.mightContain("知识点", "布隆过滤器"));
    }
}
