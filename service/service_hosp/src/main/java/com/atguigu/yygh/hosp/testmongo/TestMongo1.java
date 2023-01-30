package com.atguigu.yygh.hosp.testmongo;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author rbx
 * @title
 * @Create 2023-01-08 9:31
 * @Description
 */
@RestController
@RequestMapping("/mongo1")
public class TestMongo1 {
    @Autowired
    private MongoTemplate mongoTemplate;

    //添加
    @GetMapping("/create")
    public void create(){
        User user = new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("4932200@qq.com");
        User insert = mongoTemplate.insert(user);
        System.out.println("insert = " + insert);
    }

    //查询所有
    @GetMapping("/findAll")
    public void findAll() {
        List<User> all = mongoTemplate.findAll(User.class);
        all.forEach(System.out::println);
    }

    //根据id查询
    @GetMapping("/findId")
    public void getById() {
        User byId = mongoTemplate.findById("63ba1ec1dad0f87c939ff645", User.class);
        System.out.println("byId = " + byId);
    }

    //条件查询
    @GetMapping("/findUser")
    public void findUserList() {
        Query query = new Query(
                Criteria.where("name").is("test")
                        .and("age").is(20)
        );
        List<User> users = mongoTemplate.find(query, User.class);
        users.forEach(System.out::println);
    }

    //模糊查询
    @GetMapping("/findLike")
    public void findLike() {
        String name = "est";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query = new Query(
                Criteria.where("name").regex(pattern)
        );

        List<User> users = mongoTemplate.find(query, User.class);
        users.forEach(System.out::println);
    }

    //分页查询
    @GetMapping("findPage")
    public void findUsersPage() {
        String name = "est";
        int pageNo = 1;
        int pageSize = 10;

        Query query = new Query();
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        query.addCriteria(Criteria.where("name").regex(pattern));
        int totalCount = (int) mongoTemplate.count(query, User.class);

        List<User> userList = mongoTemplate.find(query.skip((pageNo - 1) * pageSize).limit(pageSize), User.class);

        Map<String, Object> pageMap = new HashMap<>();
        pageMap.put("list", userList);
        pageMap.put("totalCount",totalCount);
        System.out.println(pageMap);
    }

    //修改
    @GetMapping("update")
    public void updateUser() {
        User user = mongoTemplate.findById("5ffbfa2ac290f356edf9b5aa", User.class);
        user.setName("test_1");
        user.setAge(25);
        user.setEmail("493220990@qq.com");
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        Update update = new Update();
        update.set("name", user.getName());
        update.set("age", user.getAge());
        update.set("email", user.getEmail());
        UpdateResult result = mongoTemplate.upsert(query, update, User.class);
        long count = result.getModifiedCount();
        System.out.println(count);
    }

    //删除操作
    @GetMapping("delete")
    public void delete() {
        Query query =
                new Query(Criteria.where("_id").is("5ffbfa2ac290f356edf9b5aa"));
        DeleteResult result = mongoTemplate.remove(query, User.class);
        long count = result.getDeletedCount();
        System.out.println(count);
    }


}
