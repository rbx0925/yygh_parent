package com.atguigu.yygh.hosp.testmongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-08 10:27
 * @Description
 */
@RestController
@RequestMapping("/mongo2")
public class TestMongo2 {
    @Autowired
    private UserRepository userRepository;

    //添加
    @GetMapping("create")
    public void createUser() {
        User user = new User();
        user.setAge(20);
        user.setName("张三");
        user.setEmail("3332200@qq.com");
        User user1 = userRepository.save(user);
        System.out.println("user1 = " + user1);
    }

    //查询所有
    @GetMapping("findAll")
    public void findUser() {
        List<User> userList = userRepository.findAll();
        System.out.println(userList);
    }

    //id查询
    @GetMapping("findId")
    public void getById() {
        User user = userRepository.findById("60b8d57ed539ed5b124942de").get();
        System.out.println(user);
    }

    //条件查询
    @GetMapping("findQuery")
    public void findUserList() {
        User user = new User();
        user.setName("张三");
        user.setAge(20);
        Example<User> userExample = Example.of(user);
        List<User> userList = userRepository.findAll(userExample);
        System.out.println(userList);
    }

    //模糊查询
    @GetMapping("findLike")
    public void findUsersLikeName() {
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        User user = new User();
        user.setName("三");
        Example<User> userExample = Example.of(user, matcher);
        List<User> userList = userRepository.findAll(userExample);
        System.out.println(userList);
    }

    //分页查询
    @GetMapping("findPage")
    public void findUsersPage() {
        Sort sort = Sort.by(Sort.Direction.DESC, "age");
        //0为第一页
        Pageable pageable = PageRequest.of(0, 10, sort);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        User user = new User();
        user.setName("三");
        Example<User> userExample = Example.of(user, matcher);
        //创建实例
        Example<User> example = Example.of(user, matcher);
        Page<User> pages = userRepository.findAll(example, pageable);
        System.out.println(pages);
    }

    //修改
    @GetMapping("update")
    public void updateUser() {
        User user = userRepository.findById("60b8d57ed539ed5b124942de").get();
        user.setName("张三_1");
        user.setAge(25);
        user.setEmail("883220990@qq.com");
        User save = userRepository.save(user);
        System.out.println(save);
    }

    //删除
    @GetMapping("delete")
    public void delete() {
        userRepository.deleteById("60b8d57ed539ed5b124942de");
    }

    @GetMapping("/testMethod1")
    public void testMethod1() {
        List<User> userList =userRepository.getByNameAndAge("张三_1",20);
    }

    @GetMapping("/testMethod2")
    public void testMethod2() {
        List<User> userList = userRepository.getByNameLike("三");
    }
}
